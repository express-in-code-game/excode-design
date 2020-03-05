(ns ui.alpha.tests
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
   [common.alpha.core :refer [rand-uuid]]
   
   [common.alpha.tests]
   [common.sample-tests]
   [ui.alpha.spec-test]
   [ui.sample-tests]))

(deftest common-deps
  (testing "generating random uuid via reader conditionals in .cljc"
    (is (uuid? (rand-uuid)))))

(comment

  (do
    (run-tests)
    (run-tests
     'common.sample-tests
     'common.alpha.tests
     'ui.sample-tests
     'ui.alpha.spec-test))

  (run-all-tests #"ui.*")
  (re-matches #"ui.*" "ui.alpha.spec-test")
  (stest/check)
  (tc/quick-check)

  ;;
  )
