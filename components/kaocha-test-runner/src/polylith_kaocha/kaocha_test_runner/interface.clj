(ns polylith-kaocha.kaocha-test-runner.interface
  (:require
    [polylith-kaocha.kaocha-test-runner.core :as core]))

(defn create [opts]
  (core/create opts))
