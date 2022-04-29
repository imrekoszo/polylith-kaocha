(ns polylith-kaocha.util.core)

(def rrapply-fn-form-
  `(fn ~'rrapply [~'sym ~'args]
     (if-some [var# (requiring-resolve ~'sym)]
       (apply var# ~'args)
       (throw (Exception. (str "Unable to resolve " ~'sym " to a var."))))))

(defmacro rrapply-fn- [] rrapply-fn-form-)

(def rrapply- (rrapply-fn-))

(defn rrapply-call-form- [sym-form arg-forms]
  (list rrapply-fn-form- sym-form (vec arg-forms)))
