(ns polylith-kaocha.kaocha-test-runner.core
  (:require
   [net.cgrand.xforms :as x]
   [polylith-kaocha.util.interface :as util]
   [polylith.clj.core.test-runner-contract.interface :as test-runner-contract]))

(defn path-to-consider-for-test?-fn [projects-to-test]
  (let [nil-or-project-to-test? (some-fn nil? (set projects-to-test))]
    (fn path-to-consider? [path]
      (->> path
        (re-find #"^projects/([^/]+)")
        (second)
        (nil-or-project-to-test?)))))

(defn runner-opts->kaocha-poly-opts
  [{:keys [#_workspace project changes test-settings color-mode is-verbose]}]
  (let [{:keys [name paths project-dir]} project
        {:keys [project-to-projects-to-test]} changes
        projects-to-test (get project-to-projects-to-test name)
        path-to-consider-for-test? (path-to-consider-for-test?-fn projects-to-test)
        src-paths (filterv path-to-consider-for-test? (:src paths))
        test-paths (filterv path-to-consider-for-test? (:test paths))]
    (cond-> {:src-paths src-paths
             :test-paths test-paths
             :is-verbose is-verbose
             :is-colored (not= "none" color-mode)
             :project-dir project-dir}

      (contains? test-settings :polylith-kaocha/config-resource)
      (assoc :config-resource (:polylith-kaocha/config-resource test-settings))

      (contains? test-settings :polylith-kaocha.kaocha-wrapper/post-load-config)
      (assoc :post-load-config (:polylith-kaocha.kaocha-wrapper/post-load-config test-settings))

      (contains? test-settings :polylith-kaocha.kaocha-wrapper/post-enhance-config)
      (assoc :post-enhance-config (:polylith-kaocha.kaocha-wrapper/post-enhance-config test-settings)))))

(defn create
  "Returns a test TestRunner which does its job by invoking functions
  from the `polylith-kaocha.kaocha-wrapper` (polylith) interface.

  The project under test must therefore have an implementation of that
  interface. This is best achieved by adding a test dependency on
  `polylith-kaocha/kaocha-wrapper` to the project.

  Configuration can be provided in a Kaocha test edn file, which must
  be available as a resource in the project under test. The resource can be
  specified in the test settings part of workspace.edn:

  ```
  {:test {:polylith-kaocha/config-resource \"path/from/classpath/root\"} ; global
   :projects
   {:foo {:test {:polylith-kaocha/config-resource \"path/from/classpath/root\"}} ; project-specific
   }}
  ```"
  [{:keys [test-settings is-verbose] :as runner-opts}]
  (let [{:polylith-kaocha/keys [runner-opts->kaocha-poly-opts
                                tests-present?
                                run-tests]
         :or {tests-present? 'polylith-kaocha.kaocha-wrapper.interface.test-plan/tests-present?
              run-tests 'polylith-kaocha.kaocha-wrapper.interface.runner/run-tests
              runner-opts->kaocha-poly-opts `runner-opts->kaocha-poly-opts}}
        test-settings

        kaocha-poly-opts (util/rrapply runner-opts->kaocha-poly-opts runner-opts)
        tests-present?-form (util/rrapply-call-form `'~tests-present? `'~kaocha-poly-opts)
        run-tests-form (util/rrapply-call-form `'~run-tests `'~kaocha-poly-opts)]
    (reify test-runner-contract/TestRunner
      (test-runner-name [_] "Kaocha test runner")

      (test-sources-present? [_]
        (->> kaocha-poly-opts
          ((juxt :src-paths :test-paths))
          (x/some cat)))

      (tests-present? [_ {:keys [eval-in-project]}]
        (when is-verbose
          (println "Evaluating in project: " (pr-str tests-present?-form)))
        (eval-in-project tests-present?-form))

      (run-tests [_ {:keys [eval-in-project]}]
        (when is-verbose
          (println "Evaluating in project: " (pr-str run-tests-form)))
        (when (pos? (eval-in-project run-tests-form))
          (throw (Exception. "\nTests failed.")))))))
