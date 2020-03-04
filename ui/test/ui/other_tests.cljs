(ns ui.other-tests
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]))

(comment

  (run-tests)
  (run-all-tests #"ui.*")
  (re-matches #"ui.*" "ui.alpha.spec-test")
  (stest/check)
  (tc/quick-check)

  ;;
  )

(deftest exploratory
  (testing "macros"
    (is (= (macroexpand '(when (pos? a) (println "positive") (/ b a)))
           '(if (pos? a) (do (println "positive") (/ b a))))
        "macroexpand example with when macro")))

