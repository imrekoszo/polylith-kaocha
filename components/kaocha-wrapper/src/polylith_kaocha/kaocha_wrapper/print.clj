(ns polylith-kaocha.kaocha-wrapper.print)

(defn verbose-prn [x label {:keys [is-verbose] :as _opts}]
  (when is-verbose
    (println
      (str "[polylith-kaocha] " label ":")
      (pr-str x))))
