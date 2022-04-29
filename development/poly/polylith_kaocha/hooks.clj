(ns polylith-kaocha.hooks
  (:require
   [polylith-kaocha.kaocha-test-runner.core :as kaocha-test-runner]))

(defn runner-opts->kaocha-poly-opts [runner-opts]
  (println "custom runner-opts->kaocha-poly-opts")
  (kaocha-test-runner/runner-opts->kaocha-poly-opts runner-opts))
