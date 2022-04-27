(ns polylith-kaocha.kaocha-wrapper.config
  (:require
   [clojure.java.io :as io]
   [kaocha.config]
   [kaocha.plugin]
   [polylith-kaocha.kaocha-wrapper.plugin-util :as plugin-util]))

(defn load-config-resource [resource-name]
  (let [resource-name (or resource-name "polylith-kaocha/kaocha-wrapper/default-tests.edn")]
    (-> resource-name
      (io/resource)
      (doto (when-not (throw (Exception. (str "Kaocha config resource " resource-name " could not be found")))))
      (kaocha.config/load-config))))

(defn with-hooks [config]
  (plugin-util/with-configured-plugins
    config
    (kaocha.plugin/run-hook :kaocha.hooks/config config)))

(defn with-overridden-paths [{:keys [src-paths test-paths]}]
  (fn [test]
    (cond-> test
      (contains? test :kaocha/source-paths)
      (assoc :kaocha/source-paths src-paths)

      (contains? test :kaocha/test-paths)
      (assoc :kaocha/test-paths test-paths))))

(defn with-poly-paths [config opts]
  (update config :kaocha/tests #(mapv (with-overridden-paths opts) %)))

(defn load-poly-prepared-config [{:keys [config-resource] :as opts}]
  (-> config-resource
    (load-config-resource)
    (with-hooks)
    (with-poly-paths opts)))
