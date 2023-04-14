(ns polylith-kaocha.kaocha-resource-config-loader.interface-test
  (:require
    [clojure.spec.alpha :as s]
    [clojure.test :refer :all]
    [kaocha.config]
    [polylith-kaocha.kaocha-config-loader.interface :as sut]))

(deftest default-config-is-the-empty-one
  (is (= (kaocha.config/load-config) (sut/load-config nil))))

(deftest throws-on-nonexistent-resource
  (is (thrown-with-msg? Exception #"Kaocha config resource not/found/at/all.edn could not be found"
        (sut/load-config "not/found/at/all.edn"))))

(deftest loads-specified-config
  (let [cfg (sut/load-config "polylith-kaocha/kaocha-resource-config-loader/test-tests.edn")]

    (testing "is valid config"
      (is (s/valid? :kaocha/config cfg)))

    (testing "is the config specified in the resource"
      (is (= :foo (:custom/attribute cfg))))))
