(ns polylith-kaocha.kaocha-test-runner.core
  (:require
    [net.cgrand.xforms :as x]
    [polylith.clj.core.test-runner-contract.interface :as test-runner-contract]))

(defn path-to-consider-for-test?-fn [projects-to-test]
  (let [nil-or-project-to-test? (some-fn nil? (set projects-to-test))]
    (fn path-to-consider? [path]
      (->> path
        (re-find #"^projects/([^/]+)")
        (second)
        (nil-or-project-to-test?)))))

(defn create
  [{:keys [#_workspace project changes #_test-settings]}]
  (let [{:keys [name paths]} project
        {:keys [project-to-projects-to-test]} changes
        projects-to-test (get project-to-projects-to-test name)
        path-to-consider-for-test? (path-to-consider-for-test?-fn projects-to-test)
        src-paths (filterv path-to-consider-for-test? (:src paths))
        test-paths (filterv path-to-consider-for-test? (:test paths))
        test-sources-present (x/some cat [src-paths test-paths])

        ;; TODO: assembling a config should be customizable from the workspace under test
        ;; so we should prepare to generate this base config inside the eval
        ->test-plan-config (memoize
                             (fn [eval-in-project]
                               (eval-in-project
                                 `(do
                                    {
                                     ;; TODO: spec test check plugin doesn't want to work but not getting any error for some reason
                                     #_#_:kaocha/plugins [:kaocha.plugin/randomize
                                                          :kaocha.plugin/filter
                                                          :kaocha.plugin/capture-output
                                                          :kaocha.plugin/profiling
                                                          #_:kaocha.plugin.alpha/spec-test-check]
                                     :kaocha/tests [{:kaocha.testable/type :kaocha.type/clojure.test
                                                     :kaocha.testable/id :unit
                                                     :kaocha/ns-patterns ["-test$"]
                                                     :kaocha/source-paths ~src-paths
                                                     :kaocha/test-paths ~test-paths
                                                     :kaocha.filter/skip-meta [:kaocha/skip]}
                                                    #_{:kaocha.testable/type :kaocha.type/spec.test.check
                                                       :kaocha.testable/id :generative-fdef-checks}]}))))]
    (reify test-runner-contract/TestRunner
      (test-runner-name [_] "Kaocha test runner example")

      (test-sources-present? [_] test-sources-present)

      (tests-present? [this {:keys [eval-in-project] :as _opts}]
        (and (test-runner-contract/test-sources-present? this)
          (let [config (->test-plan-config eval-in-project)]
            (eval-in-project
              `(do (require 'kaocha.api 'kaocha.testable 'kaocha.hierarchy)
                 (->> (kaocha.api/test-plan ~config)
                   (kaocha.testable/test-seq)
                   (some (some-fn kaocha.hierarchy/leaf? :kaocha.testable/load-error))))))))

      (run-tests [this {:keys [color-mode eval-in-project is-verbose] :as opts}]
        (when (test-runner-contract/tests-present? this opts)
          (let [config (->test-plan-config eval-in-project)
                {:kaocha.result/keys [error fail]}
                (eval-in-project
                  `(do
                     (require 'kaocha.repl 'kaocha.api 'kaocha.result)
                     (-> {;; TODO: should verbose be the documentation one and non-verbose dots?
                          :kaocha/reporter [kaocha.report/documentation
                                            ~@(when is-verbose ['kaocha.report/debug])]
                          :kaocha/color? ~(not= "none" color-mode)}
                       (merge ~config)
                       (kaocha.repl/config)
                       (kaocha.api/run)
                       (:kaocha.result/tests)
                       (kaocha.result/totals))))]
            (when (or (some nil? [error fail]) (< 0 error) (< 0 fail))
              (throw (Exception. "\nTests failed.")))))))))
