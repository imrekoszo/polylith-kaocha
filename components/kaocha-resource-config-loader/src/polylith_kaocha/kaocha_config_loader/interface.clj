(ns polylith-kaocha.kaocha-config-loader.interface
  (:require
   [polylith-kaocha.kaocha-config-loader.core :as core]))

(defn load-config
  "Uses `kaocha.config/load-config` to load a config from a resource on the classpath.

  `source` must be a valid name to a resource on the classpath or falsey to load the default config"
  [source]
  (core/load-config-resource source))
