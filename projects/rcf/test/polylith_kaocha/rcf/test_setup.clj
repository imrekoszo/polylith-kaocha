(ns polylith-kaocha.rcf.test-setup
  (:require [hyperfiddle.rcf]
            [polylith-kaocha.kaocha-wrapper.config :as config]))

(defn setup
  "Enable rcf tests https://github.com/hyperfiddle/rcf#usage"
  [project-name]
  (println "Setup hyperfiddle.rcf in project" project-name)
  (hyperfiddle.rcf/enable!))

(defn post-enhance-config [config opts]
  (println "custom post-enhance-config")
  (let [{:keys [src-paths test-paths]} opts
        opts (assoc opts
                    :test-paths
                    (into [] (concat src-paths test-paths)))
        new-config (config/default-post-enhance-config config opts)]
    new-config))
