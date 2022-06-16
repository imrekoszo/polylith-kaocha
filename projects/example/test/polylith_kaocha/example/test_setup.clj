(ns polylith-kaocha.example.test-setup)

(defn setup [project-name]
  (println (str "--- test setup for " project-name " ---")))

(defn teardown [project-name]
  (println (str "--- test teardown for " project-name " ---")))
