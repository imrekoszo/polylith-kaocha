(ns polylith-kaocha.test-runner.core-test
  (:require
   [clojure.test :refer :all]
   [polylith-kaocha.test-runner.core :as sut]
   [polylith.clj.core.test-runner-plugin.interface.initializer :as test-runner-initializer]))

(deftest default-test-runner-is-valid
  (let [ctor (test-runner-initializer/->constructor-var `sut/make)]
    (is (test-runner-initializer/valid-constructor-var? ctor))
    (is (test-runner-initializer/ensure-valid-test-runner ((deref ctor) {})))))
