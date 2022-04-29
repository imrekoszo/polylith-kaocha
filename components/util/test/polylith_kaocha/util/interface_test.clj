(ns polylith-kaocha.util.interface-test
  (:require
   [clojure.test :refer :all]
   [polylith-kaocha.util.interface :as sut]))

(defn identities [& args] args)

(deftest rrapply-test
  (testing "throws when sym cannot be requiring-resolved"
    (is (thrown? Exception (sut/rrapply nil)))
    (is (thrown? Exception (sut/rrapply 1234)))
    (is (thrown? Exception (sut/rrapply 'unqualified)))
    (is (thrown? Exception (sut/rrapply 'totally.incorrect.namespace/foo)))
    (is (thrown? Exception (sut/rrapply `good-ns-bad-symbol))))

  (testing "calls resolved var as a function, passing args"
    (is (empty? (sut/rrapply `identities)))
    (is (= [1] (sut/rrapply `identities 1)))
    (is (= [1 2 3] (sut/rrapply `identities 1 2 3)))))

(deftest rrapply-call-form-test
  (let [sym `identity
        arg {:foo ["a"] :bar true :baz 'asdf}]
    (testing "forms from bindings"
      (is (= arg
            (eval
              (sut/rrapply-call-form
                `'~sym
                `'~arg)))))
    (testing "forms inline"
      (is (= arg
            (eval
              (sut/rrapply-call-form
                '`identity
                '{:foo ["a"]
                  :bar true
                  :baz 'asdf})))))

    (testing "multiple args"
      (is (= [arg arg]
            (eval
              (sut/rrapply-call-form
                '`identities
                `'~arg
                '{:foo ["a"]
                  :bar true
                  :baz 'asdf})))))))
