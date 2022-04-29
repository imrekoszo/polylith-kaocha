(ns polylith-kaocha.example.hooks
  (:require
   [polylith-kaocha.kaocha-wrapper.config :as config]
   [polylith-kaocha.kaocha-wrapper.interface.runner :as runner]
   [polylith-kaocha.kaocha-wrapper.interface.test-plan :as test-plan]))

(defn post-process-config [config opts]
  (println "custom post-process config")
  (config/default-post-process-config config opts))

(defn tests-present? [opts]
  (println "custom tests-present?")
  (test-plan/tests-present? opts))

(defn run-tests [opts]
  (println "custom run-tests")
  (runner/run-tests opts))
