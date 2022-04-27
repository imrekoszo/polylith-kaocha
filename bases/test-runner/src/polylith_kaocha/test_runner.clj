(ns polylith-kaocha.test-runner
  (:require
   [polylith-kaocha.kaocha-test-runner.interface :as kaocha-test-runner]))

(defn create [opts]
  (kaocha-test-runner/create opts))
