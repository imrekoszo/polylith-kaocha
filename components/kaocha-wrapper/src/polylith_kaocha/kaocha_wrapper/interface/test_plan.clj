(ns polylith-kaocha.kaocha-wrapper.interface.test-plan
  (:require
   [polylith-kaocha.kaocha-wrapper.test-plan :as core]))

(defn tests-present? [opts]
  (core/tests-present? opts))
