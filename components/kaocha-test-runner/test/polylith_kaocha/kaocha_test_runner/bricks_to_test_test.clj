(ns polylith-kaocha.kaocha-test-runner.bricks-to-test-test
  (:require
    [clojure.test :refer :all]
    [polylith-kaocha.kaocha-test-runner.bricks-to-test :as sut]))

(deftest
  ^{:doc "Regression test.
  Extracted from a run of `clojure -Srepro -M:poly test` at https://github.com/imrekoszo/polylith-kaocha-issue-repro/tree/f16f3a4e58d61c3e916bada6736fafbb1d45cff7"}
  bricks-to-test-test
  (let [args-0-2-18
        [{:base-names {:src ["d"], :test ["d"]},
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
                                                  "development" {:src ["d"], :test ["d"]}}}}]

        args-0-2-19
        [{:base-names {:src ["d"], :test ["d"]},
          :is-dev false,
          :name "p",
          :indirect-changes {:src ["d"], :test ["d"]},
          :alias "p",
          :projects-to-test [],
          :component-names {:src ["a" "b" "c" "s"], :test ["a" "b" "c"]},
          :test #:polylith-kaocha{:config-resource "tests.edn"},
          :bricks-to-test ["a" "b" "c" "d"],}
         {:projects [{:base-names {:src ["d"], :test ["d"]},
                      :is-dev false,
                      :name "p",
                      :type "project",
                      :indirect-changes {:src ["d"], :test ["d"]},
                      :alias "p",
                      :projects-to-test [],
                      :component-names {:src ["a" "b" "c" "s"], :test ["a" "b" "c"]},
                      :test #:polylith-kaocha{:config-resource "tests.edn"},
                      :bricks-to-test ["a" "b" "c" "d"],}
                     {:base-names {},
                      :is-dev false,
                      :name "r",
                      :type "project",
                      :indirect-changes {:src [], :test []},
                      :alias "r",
                      :projects-to-test [],
                      :component-names {:src ["c"], :test ["c"]},
                      :test #:polylith-kaocha{:config-resource "tests.edn"},
                      :bricks-to-test ["c"]}
                     {:base-names {:src ["d"], :test ["d"]},
                      :is-dev true,
                      :name "development",
                      :type "project",
                      :indirect-changes {:src ["d"], :test ["d"]},
                      :alias "dev",
                      :projects-to-test [],
                      :component-names {:src ["a" "b" "c" "s"], :test ["a" "b" "c"]},
                      :test #:polylith-kaocha{:config-resource "tests.edn"},
                      :bricks-to-test []}],
          :user-input {:args ["test"],
                       :unnamed-args [],
                       :is-dev false,
                       :is-commit false,
                       :is-run-project-tests false,
                       :is-compact false,
                       :is-run-all-brick-tests false,
                       :is-fake-poly false,
                       :is-verbose false,
                       :is-latest-sha false,
                       :selected-projects #{},
                       :is-update false,
                       :is-github false,
                       :is-no-changes false,
                       :is-search-for-ws-dir false,
                       :is-swap-axes false,
                       :is-outdated false,
                       :is-local false,
                       :is-show-brick false,
                       :is-show-resources false,
                       :selected-profiles #{},
                       :cmd "test",
                       :is-show-project false,
                       :is-all false,
                       :is-show-workspace false,
                       :is-tap false,
                       :is-show-loc false,
                       :is-no-exit false},
          :settings {:top-namespace "fun",
                     :default-profile-name "default",
                     :thousand-separator ",",
                     :compact-views #{},
                     :active-profiles #{},
                     :interface-ns "interface",
                     :tag-patterns {:stable "stable-*", :release "v[0-9]*"},
                     :color-mode "dark",
                     :empty-character "."},
          :ws-type "toolsdeps2",
          :changes {:since "stable",
                    :changed-projects ["development" "p" "r"],
                    :changed-bases [],
                    :since-sha "46bcf2c0c7eebfc54c074d43290a81d14d2da614",
                    :changed-files [".gitignore"
                                    "components/a/src/fun/a/interface.clj"
                                    "components/s/src/fun/s/interface.clj"
                                    "deps.edn"
                                    "development/config.edn"
                                    "projects/p/config.edn"
                                    "projects/p/deps.edn"
                                    "projects/r/config.edn"
                                    "projects/r/deps.edn"
                                    "workspace.edn"],
                    :git-diff-command "git diff 46bcf2c0c7eebfc54c074d43290a81d14d2da614 --name-only",
                    :changed-components ["a" "s"],
                    :since-tag "stable-3",
                    :changed-or-affected-projects ["development" "p" "r"]},
          :profiles []}]]
    (testing "bricks that do not have test files are also selected"
      (let [expected #{"d" "s" "a" "b" "c"}]

        (testing "up to poly v0.2.18"
          (is (= expected (apply sut/bricks-to-test args-0-2-18))))

        (testing "from poly v0.2.19"
          (is (= expected (apply sut/bricks-to-test args-0-2-19))))))

    (testing "include bricks"
      (let [expected #{"b" "c"}]

        (testing "up to poly v0.2.18"
          (let [with-include #(assoc-in % [1 :settings :projects "p" :test :include] ["b" "c"])]
            (is (= expected
                  (apply sut/bricks-to-test (with-include args-0-2-18))))))

        (testing "from poly v0.2.19"
          (let [with-include #(assoc-in % [0 :test :include] ["b" "c"])]
            (is (= expected
                  (apply sut/bricks-to-test (with-include args-0-2-19))))))))))
