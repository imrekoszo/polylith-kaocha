(ns polylith-kaocha.example.core
  (:require
   [clojure.spec.alpha :as s]))

(defn add [a b]
  (+' a b))

(s/def ::number
  (s/and number? (complement (every-pred double? (some-fn NaN? infinite?)))))

(s/fdef add
  :args (s/cat :a ::number :b ::number)
  :ret ::number
  :fn (fn [{:keys [args ret]}]
        (== ret (+' (:a args) (:b args)))))
