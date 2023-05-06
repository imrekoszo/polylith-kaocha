(ns polylith-kaocha.kaocha-wrapper.runner
  (:require
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [kaocha.api]
   [kaocha.plugin]
   [kaocha.report]
   [kaocha.result]
   [kaocha.specs]
   [polylith-kaocha.kaocha-wrapper.config :as config]
   [polylith-kaocha.kaocha-wrapper.print :as print]
   [slingshot.slingshot :refer [try+]]))

(defn with-debug-reporter [kaocha-reporter]
  (assert (s/valid? :kaocha/reporter kaocha-reporter))
  (as-> kaocha-reporter $
    (cond-> $
      (not (vector? $))
      (vector))
    (cond-> $
      (not-any? #{kaocha.report/debug} $)
      (conj kaocha.report/debug))))

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

(defn run-with-complete-config [config opts]
  (kaocha.plugin/run-hook :kaocha.hooks/main config)
  (let [{:kaocha.result/keys [error fail]}
        (-> config
          (run-to-test-results!)
          (doto (when-not (throw (Exception. "Unable to create test summary."))))
          (kaocha.result/totals)
          (doto (print/verbose-prn "kaocha.result" opts)))]
    (+ error fail)))

(defn in-context-runner [opts]
  (fn run- [config]
    (binding [s/*explain-out* expound/printer]
      (-> config
        (with-verbosity opts)
        (with-color opts)
        (doto (print/verbose-prn "complete config" opts))
        (run-with-complete-config opts)))))

(defn run-tests-with-kaocha [opts]
  (try+
    (config/execute-in-config-context opts (in-context-runner opts))
    (catch :kaocha/early-exit early-exit
      (-> early-exit
        (doto (print/verbose-prn "kaocha/early-exit" opts))
        (:kaocha/early-exit)))))

(defn run-tests [opts]
  (try
    (->
      (run-tests-with-kaocha opts)
      (doto (print/verbose-prn "run-tests-with-kaocha" opts)))
    (catch Throwable e
      (-> e
        (doto (print/verbose-prn "run-tests-with-kaocha threw" opts))
        (throw)))))
