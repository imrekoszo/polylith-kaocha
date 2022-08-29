(ns polylith-kaocha.kaocha-wrapper.interface.runner
  (:require
   [polylith-kaocha.kaocha-wrapper.runner :as core]))

(defn run-tests [opts]
  (println "Run tests" opts)
  (core/run-tests opts))
