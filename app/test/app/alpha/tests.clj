(ns app.alpha.tests
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.spec.test.alpha :as stest]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]

            [app.alpha.streams.game-test]
            [app.alpha.streams.user-test]
            [app.alpha.streams.core-test]))

(deftest hello
  (testing "Airhtmetic"
    (is (= 5 (+ 3 2)))))

(comment
  
  (run-tests)

  (run-all-tests #"app.*")
  (re-matches #"app.*" "app.alpha.streams.game-test")
  (stest/check)
  (tc/quick-check)

  ;;
  )
