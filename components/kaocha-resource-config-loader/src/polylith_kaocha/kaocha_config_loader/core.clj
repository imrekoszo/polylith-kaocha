(ns polylith-kaocha.kaocha-config-loader.core
  (:require
   [clojure.java.io :as io]
   [kaocha.config]))

(defn load-config-resource [resource-name]
  (let [resource-name (or resource-name "polylith-kaocha/kaocha-resource-config-loader/default-tests.edn")]
    (-> resource-name
        (io/resource)
        (doto (when-not (throw (Exception. (str "Kaocha config resource " resource-name " could not be found")))))
        (kaocha.config/load-config))))
