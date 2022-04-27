(ns polylith-kaocha.kaocha-wrapper.config-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [polylith-kaocha.kaocha-wrapper.config :as sut]))

(deftest load-config-resource
  (testing "loads default when passed nil"
    (is (s/valid? :kaocha/config (sut/load-config-resource nil))))

  (testing "loads specified when passed"
    (let [config (sut/load-config-resource "polylith-kaocha/kaocha-wrapper/test-tests.edn")]
      (is (s/valid? :kaocha/config config))
      (is (true? (:kaocha/fail-fast? config)))))

  (testing "throws when specified config cannot be found"
    (is (thrown? Exception (sut/load-config-resource "not/found/at/all.edn")))))
