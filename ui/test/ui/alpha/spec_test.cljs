(ns ui.alpha.spec-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
   [ui.alpha.spec]))

(comment

  (run-tests)

  ;;
  )

(deftest sample-tests
  (testing "relationships created with (derive )"
    (is (isa? (type :a-keyword) :isa/keyword)
        ":a-keyword is a :isa/keyword")))