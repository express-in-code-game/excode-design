(ns ui.alpha.spec-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   #_[cljs.spec.test.alpha :refer-macros [check enumerate-namespace]]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :as test :refer [is testing run-tests deftest]]
   [ui.alpha.spec]))

(comment

  (run-tests)

  ;;
  )

(deftest sample-tests
  (testing "relationships created with (derive )"
    (is (isa? (type :a-keyword) :isa/keyword)
        ":a-keyword is a :isa/keyword")))