(ns polylith-kaocha.kaocha-wrapper.plugin-util
  (:require
   [kaocha.plugin :as plugin]))

(defmacro with-configured-plugins
  "Not sure why this isn't in Kaocha itself,
  it appears to be called from multiple places within it."
  [config & body]
  `(plugin/with-plugins
     (plugin/load-all (:kaocha/plugins ~config))
     ~@body))
