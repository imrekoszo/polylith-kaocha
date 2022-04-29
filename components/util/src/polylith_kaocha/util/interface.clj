(ns polylith-kaocha.util.interface
  (:require
   [polylith-kaocha.util.core :as core]))

(defn rrapply
  "requiring-resolves `sym` and invokes it as a function, applying `args`

  Throws if requiring-resolve does not return a var."
  [sym & args]
  (core/rrapply- sym args))

(defn rrapply-call-form
  "returns a form that represents a call to a fn identical to rrapply"
  [sym-form & arg-forms]
  (core/rrapply-call-form- sym-form arg-forms))
