(ns polylith-kaocha.example.core-test
  (:require
   [clojure.test :refer :all]
   [polylith-kaocha.example.core :as sut]))

(deftest add-test
  (is (= 3 (sut/add 1 2))))
