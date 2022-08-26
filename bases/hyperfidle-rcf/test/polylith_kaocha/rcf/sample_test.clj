(ns polylith-kaocha.rcf.sample-test
  (:require [hyperfiddle.rcf :refer [tests]]))

(tests

 "another one bites the dust"
 (= 1 1) := true

 (+ 1 1) := 2)