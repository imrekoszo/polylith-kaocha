(ns polylith-kaocha.kaocha-test-runner.core
  (:require
   [babashka.fs :as fs]
   [net.cgrand.xforms :as x]
   [polylith-kaocha.util.interface :as util]
   [polylith.clj.core.test-runner-contract.interface :as test-runner-contract]))

(defn brick-paths [{:keys [name type paths] :as _brick}]
  (->> paths
    ((juxt :src :test))
    (eduction
      (comp cat
        (map #(str (fs/path (str type "s") name %)))))))

(defmacro verbose-println [runner-opts & args]
  `(when (:is-verbose ~runner-opts)
     (println "[polylith-kaocha]" ~@args)))

(defn changed-brick-path?-fn
  [{:keys [workspace project] :as runner-opts}]
  (let [{:keys [bases components]} workspace
        bricks-to-test (set (:bricks-to-test-all-sources project))
        brick-paths (into #{}
                      (comp cat
                        (filter #(contains? bricks-to-test (:name %)))
                        (mapcat brick-paths))
                      [bases components])]
    (verbose-println runner-opts "Bricks to test:" (sort bricks-to-test))
    (verbose-println runner-opts "Brick paths:" (sort brick-paths))

    (fn changed-brick-path? [path]
      (doto
        (contains? brick-paths path)
        (-> (if "is a brick path to consider for Kaocha's test discovery"
              "is NOT considered for Kaocha's test discovery")
          (->> (verbose-println runner-opts "Path" path)))))))

(defn project-to-test-0-2-19
  [{:keys [name projects-to-test] :as _project}
   {:keys [project-to-projects-to-test] :as _changes}]
  (or projects-to-test ;; poly version > 0.2.18
    (project-to-projects-to-test name) ;; poly version up to 0.2.18
    ))

(defn path-of-project-to-test?-fn
  [{:keys [project changes] :as _runner-opts}]
  (let [projects-to-test (set (project-to-test-0-2-19 project changes))]
    (fn path-of-project-to-test? [path]
      (->> path
        (re-find #"^projects/([^/]+)")
        (second)
        (contains? projects-to-test)))))

(defn path-to-consider-for-test?-fn [runner-opts]
  (some-fn
    (changed-brick-path?-fn runner-opts)
    (path-of-project-to-test?-fn runner-opts)))

(defn runner-opts->kaocha-poly-opts
  [{:keys [#_workspace project #_changes test-settings color-mode is-verbose] :as runner-opts}]
  (let [{:keys [paths project-dir]} project
        path-to-consider-for-test? (path-to-consider-for-test?-fn runner-opts)
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

(defn apply-in-project [{:keys [eval-in-project] :as runner-opts} remote-fn-sym kaocha-poly-opts]
  (let [form (util/rrapply-call-form `'~remote-fn-sym `'~kaocha-poly-opts)]
    (verbose-println runner-opts "Evaluating in project: " (pr-str form))
    (eval-in-project form)))

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
  [{:keys [test-settings] :as runner-opts}]
  (let [{:polylith-kaocha/keys [runner-opts->kaocha-poly-opts
                                tests-present?
                                run-tests]
         :or {tests-present? 'polylith-kaocha.kaocha-wrapper.interface.test-plan/tests-present?
              run-tests 'polylith-kaocha.kaocha-wrapper.interface.runner/run-tests
              runner-opts->kaocha-poly-opts `runner-opts->kaocha-poly-opts}}
        test-settings

        kaocha-poly-opts* (delay (util/rrapply runner-opts->kaocha-poly-opts runner-opts))]
    (reify test-runner-contract/TestRunner
      (test-runner-name [_] "polylith-kaocha")

      (test-sources-present? [_]
        (x/some cat ((juxt :src-paths :test-paths) @kaocha-poly-opts*)))

      (tests-present? [_ runner-opts]
        (apply-in-project runner-opts tests-present? @kaocha-poly-opts*))

      (run-tests [_ runner-opts]
        (-> runner-opts
          (apply-in-project run-tests @kaocha-poly-opts*)
          (zero?)
          (when-not (throw (Exception. "Tests failed [polylith-kaocha]"))))))))
