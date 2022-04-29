(ns polylith-kaocha.kaocha-wrapper.test-plan
  (:require
   [kaocha.api]
   [kaocha.hierarchy]
   [kaocha.testable]
   [polylith-kaocha.kaocha-wrapper.config :as config]))

(defn tests-present-in-config? [config]
  (->> config
    (kaocha.api/test-plan)
    (kaocha.testable/test-seq)
    (some (some-fn kaocha.hierarchy/leaf? :kaocha.testable/load-error))))

(defn tests-present? [{:keys [is-verbose] :as opts}]
  (try
    (config/execute-in-config-context opts tests-present-in-config?)
    (catch Throwable e
      (throw (doto e (->> (prn) (when is-verbose)))))))
