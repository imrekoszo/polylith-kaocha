(ns polylith-kaocha.kaocha-wrapper.test-plan
  (:require
   [kaocha.api]
   [kaocha.hierarchy]
   [kaocha.testable]
   [polylith-kaocha.kaocha-wrapper.config :as config]
   [polylith-kaocha.kaocha-wrapper.plugin-util :as plugin-util]))

(defn test-plan [config]
  (plugin-util/with-configured-plugins
    config
    (kaocha.api/test-plan config)))

(defn tests-present-in-config? [config]
  (->> config
    (test-plan)
    (kaocha.testable/test-seq)
    (some (some-fn kaocha.hierarchy/leaf? :kaocha.testable/load-error))))

(defn tests-present? [opts]
  (-> (config/load-poly-prepared-config opts)
    (tests-present-in-config?)))
