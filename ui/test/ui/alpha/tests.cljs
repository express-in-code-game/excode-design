(ns ui.alpha.tests
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]

   [ui.alpha.spec-test]
   [common.alpha.core :refer [rand-uuid]]))

(deftest common-deps
  (testing "generating random uuid via reader conditionals in .cljc"
    (is (uuid? (rand-uuid)))))

(comment

  (run-tests)

  (run-all-tests #"ui.*")
  (re-matches #"ui.*" "ui.alpha.spec-test")
  (stest/check)
  (tc/quick-check)

  ;;
  )
