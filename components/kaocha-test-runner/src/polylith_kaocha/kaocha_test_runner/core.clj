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
  [{:keys [#_workspace project changes test-settings]}]
  (let [{:keys [name paths]} project
        {:keys [project-to-projects-to-test]} changes
        {:polylith-kaocha/keys [config-resource]} test-settings
        projects-to-test (get project-to-projects-to-test name)
        path-to-consider-for-test? (path-to-consider-for-test?-fn projects-to-test)
        src-paths (filterv path-to-consider-for-test? (:src paths))
        test-paths (filterv path-to-consider-for-test? (:test paths))
        test-sources-present (x/some cat [src-paths test-paths])
        kaocha-poly-opts {:config-resource config-resource
                          :src-paths src-paths
                          :test-paths test-paths}
        tests-present?-sym 'polylith-kaocha.kaocha-wrapper.interface.test-plan/tests-present?
        run-tests-sym 'polylith-kaocha.kaocha-wrapper.interface.runner/run-tests]
    (reify test-runner-contract/TestRunner
      (test-runner-name [_] "Kaocha test runner")

      (test-sources-present? [_] test-sources-present)

      (tests-present? [_ {:keys [eval-in-project] :as _opts}]
        (eval-in-project `((requiring-resolve '~tests-present?-sym) ~kaocha-poly-opts)))

      (run-tests [_ {:keys [color-mode eval-in-project is-verbose] :as _opts}]
        (let [kaocha-poly-opts (merge kaocha-poly-opts
                                 {:is-verbose is-verbose
                                  :is-colored (not= "none" color-mode)})
              failure-error-count (eval-in-project `((requiring-resolve '~run-tests-sym) ~kaocha-poly-opts))]
          (when (pos? failure-error-count)
            (throw (Exception. "\nTests failed."))))))))
