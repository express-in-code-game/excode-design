(ns starnet.common.alpha.user-test
  (:require
   [clojure.set :refer [subset?]]
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
   [starnet.common.alpha.spec :refer [eventset-event]]))


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
    (is (subset? (into #{} (gen/sample (s/gen :ev/type))) eventset-event)
        "generate a subset of :ev/type")))

(deftest all-specchecks
  (testing "running spec.test/check via stest/enumerate-namespace"
    (let [summary (-> #?(:clj (stest/enumerate-namespace 'starnet.common.alpha.user)
                         :cljs 'starnet.common.alpha.user)
                      (stest/check {:clojure.spec.test.check/opts {:num-tests 10}})
                      (stest/summarize-results))]
      (is (not (contains? summary :check-failed))))))