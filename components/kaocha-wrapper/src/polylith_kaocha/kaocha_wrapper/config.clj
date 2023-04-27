(ns polylith-kaocha.kaocha-wrapper.config
  (:require
    [babashka.fs :as fs]
    [clojure.java.io :as io]
    [kaocha.config]
    [kaocha.plugin]
    [polylith-kaocha.kaocha-config-loader.interface :as config-loader]
    [polylith-kaocha.util.interface :as util]))

(defn with-project-orchestra-output [config {:keys [project-dir]}]
  (cond-> config
    (and project-dir (not (contains? (:cloverage/opts config) :output)))
    (assoc-in [:cloverage/opts :output] (str (fs/path project-dir "target" "cloverage")))))

(defn default-post-load-config [config opts]
  (-> config
    (with-project-orchestra-output opts)))

(defn with-post-load
  [config
   {:keys [post-load-config]
    :or {post-load-config `default-post-load-config}
    :as opts}]
  (util/rrapply post-load-config config opts))

(defn load-config [{:keys [config-resource] :as opts}]
  (-> config-resource
    (config-loader/load-config)
    (with-post-load opts)))

(defmacro with-configured-plugins
  "Not sure why this isn't in Kaocha itself, it appears to be called from multiple places within it."
  [config & body]
  `(kaocha.plugin/with-plugins
     (kaocha.plugin/load-all (:kaocha/plugins ~config))
     ~@body))

(defn with-hooks [config]
  (kaocha.plugin/run-hook :kaocha.hooks/config config))

(defn with-overridden-paths-fn [{:keys [src-paths test-paths]}]
  (fn [testable]
    (cond-> testable
      (contains? testable :kaocha/source-paths)
      (assoc :kaocha/source-paths src-paths)

      (contains? testable :kaocha/test-paths)
      (assoc :kaocha/test-paths test-paths))))

(defn with-poly-paths [config opts]
  (update config :kaocha/tests #(mapv (with-overridden-paths-fn opts) %)))

(defn default-post-enhance-config [config opts]
  (-> config
    (with-poly-paths opts)))

(defn with-post-enhance
  [config
   {:keys [post-enhance-config]
    :or {post-enhance-config `default-post-enhance-config}
    :as opts}]
  (util/rrapply post-enhance-config config opts))

(defn enhance [config opts]
  (-> config
    (with-hooks)
    (with-post-enhance opts)))

(defn execute-in-config-context
  [opts f-of-config]
  (let [config (load-config opts)]
    (with-configured-plugins config
      (let [config (enhance config opts)]
        (with-bindings (kaocha.config/binding-map config)
          (f-of-config config))))))
