(ns polylith-kaocha.kaocha-wrapper.runner
  (:require
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [kaocha.api]
   [kaocha.plugin]
   [kaocha.report]
   [kaocha.result]
   [polylith-kaocha.kaocha-wrapper.config :as config]
   [slingshot.slingshot :refer [try+]]))

(defn with-debug-reporter [kaocha-reporter]
  (cond-> kaocha-reporter
    (not-any? #{kaocha.report/debug} kaocha-reporter)
    (conj kaocha.report/debug)))

(defn with-verbosity [config {:keys [is-verbose]}]
  (cond-> config
    is-verbose
    (update :kaocha/reporter with-debug-reporter)))

(defn with-color [config {:keys [is-colored]}]
  (assoc config :kaocha/color? is-colored))

(defn run-to-test-results! [config]
  (->> config
    (kaocha.api/run)
    (kaocha.plugin/run-hook :kaocha.hooks/post-summary)
    (:kaocha.result/tests)))

(defn run-with-complete-config [config]
  (kaocha.plugin/run-hook :kaocha.hooks/main config)
  (let [{:kaocha.result/keys [error fail]}
        (-> config
          (run-to-test-results!)
          (doto (when-not (throw (Exception. "Unable to create test summary."))))
          (kaocha.result/totals))]
    (+ error fail)))

(defn in-context-runner [opts]
  (fn run- [config]
    (binding [s/*explain-out* expound/printer]
      (-> config
        (with-verbosity opts)
        (with-color opts)
        (run-with-complete-config)))))

(defn run-tests-with-kaocha [opts]
  (try+
    (config/execute-in-config-context opts (in-context-runner opts))
    (catch :kaocha/early-exit {exit-code :kaocha/early-exit}
      exit-code)))

(defn run-tests [{:keys [is-verbose] :as opts}]
  (try
    (run-tests-with-kaocha opts)
    (catch Throwable e
      (throw (doto e (->> (prn) (when is-verbose)))))))
