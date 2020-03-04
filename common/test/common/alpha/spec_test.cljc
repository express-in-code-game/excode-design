(ns common.alpha.spec-test
  (:require
   [clojure.set :refer [subset?]]
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
   [common.alpha.spec :refer [setof-ev-event]]))


(comment
  
  (run-tests)
  
  ;;
  )

(deftest spec-tests
  (is (s/valid? (s/coll-of :u/email) (gen/sample (s/gen :u/email)))
      "coll-of :u/email can be generated via custom gen")
  (testing ":g/game spec"
    (is (s/valid? :g/game (gen/generate (s/gen :g/game)))
        "game can be generated"))
  (testing "event specs"
    (is (subset? (into #{} (gen/sample (s/gen :ev/type))) setof-ev-event)
        "generate a subset of :ev/type")))
