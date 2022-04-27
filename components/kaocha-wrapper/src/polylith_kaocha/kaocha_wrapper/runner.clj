(ns polylith-kaocha.kaocha-wrapper.runner
  (:require
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [kaocha.api]
   [kaocha.plugin]
   [kaocha.report]
   [kaocha.result]
   [polylith-kaocha.kaocha-wrapper.config :as config]))

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

(defn run-tests [opts]
  (let [{:kaocha.result/keys [error fail]}
        (-> opts
          (config/load-poly-prepared-config)
          (with-verbosity opts)
          (with-color opts)
          (->>
            (kaocha.api/run)
            (binding [s/*explain-out* expound/printer])
            (kaocha.plugin/run-hook :kaocha.hooks/post-summary)
            (:kaocha.result/tests))
          (doto
            (when-not (throw (Exception. "Unable to create test summary."))))
          (kaocha.result/totals))]
    (+ error fail)))
