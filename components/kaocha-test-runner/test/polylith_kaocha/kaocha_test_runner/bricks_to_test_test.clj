(ns polylith-kaocha.kaocha-test-runner.bricks-to-test-test
  (:require
   [clojure.test :refer :all]
   [polylith-kaocha.kaocha-test-runner.bricks-to-test :as sut]))

(deftest
  ^{:doc "Regression test.
  Extracted from a run of `clojure -Srepro -M:poly test` at https://github.com/imrekoszo/polylith-kaocha-issue-repro/tree/f16f3a4e58d61c3e916bada6736fafbb1d45cff7"}
  bricks-to-test-test
  (testing "bricks that do not have test files are also selected"
    (let [expected #{"d" "s" "a" "b" "c"}]
      (testing "up to poly v0.2.18"
        (is (= expected
              (sut/bricks-to-test
                {:base-names {:src ["d"], :test ["d"]},
                 :is-dev false,
                 :name "p",
                 :type "project",
                 :alias "p",
                 :component-names {:src ["a" "b" "c" "s"], :test ["a" "b" "c"]},
                 :deps {"d" {:src {:direct ["a" "b" "c" "s"]}, :test {:direct ["a" "b" "c" "s"]}},
                        "a" {:src {:direct ["b"]}, :test {:direct ["b"]}},
                        "b" {:src {}, :test {}},
                        "c" {:src {}, :test {}},
                        "s" {:src {}, :test {}}}}
                {:user-input {:is-dev false, :is-run-all-brick-tests false, :selected-projects #{}},
                 :settings {:projects {"development" {:alias "dev", :test #:polylith-kaocha{:config-resource "tests.edn"}},
                                       "p" {:alias "p", :test #:polylith-kaocha{:config-resource "tests.edn"}},
                                       "r" {:alias "r", :test #:polylith-kaocha{:config-resource "tests.edn"}}}},
                 :changes {:changed-components ["a" "s"],
                           :changed-bases [],
                           :changed-projects ["development" "p" "r"],
                           :project-to-indirect-changes {"p" {:src ["d"], :test ["d"]},
                                                         "r" {:src [], :test []},
                                                         "development" {:src ["d"], :test ["d"]}}}})))))))
