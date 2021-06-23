(ns starnet.common.alpha.tests
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]

   [starnet.common.alpha.game-test]
   [starnet.common.alpha.core-test]))

(deftest sample-test
  (testing "Arihtmetic"
    (is (= 5 (+ 3 2)))))

(comment

  (run-tests)

  (run-all-tests #"common.*")
  (stest/check)
  (tc/quick-check)

  ;;
  )
