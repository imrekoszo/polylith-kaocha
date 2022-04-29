(ns polylith-kaocha.reload-runner
  (:require
   [polylith-kaocha.util.interface :as util]))

(defn reload [ns-sym]
  (println "Reloading " ns-sym)
  (require ns-sym :reload))

(defn create
  "This is a helper for the development of test-runner so it can be
  invoked repeatedly in a poly shell while picking up changes to it."
  [opts]
  (reload 'polylith-kaocha.kaocha-test-runner.core)
  #_(reload 'polylith.clj.core.test-runner-orchestrator.core)
  (util/rrapply 'polylith-kaocha.test-runner/create opts))
