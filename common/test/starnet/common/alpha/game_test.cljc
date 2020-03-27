(ns starnet.common.alpha.game-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :as test :refer [is testing run-tests deftest]]
   [starnet.common.alpha.game :refer [make-state-core next-state-core]]))

(deftest make-state-core-tests
  (testing "generates valid :g/state"
    (is (s/valid? :g/state
                  (make-state-core)))))

(deftest all-specchecks
  (testing "running spec.test/check via stest/enumerate-namespace"
    (let [summary (-> #?(:clj (stest/enumerate-namespace 'starnet.common.alpha.game)
                         :cljs 'starnet.common.alpha.game)
                      (stest/check {:clojure.spec.test.check/opts {:num-tests 10}})
                      (stest/summarize-results))]
      (is (not (contains? summary :check-failed))))))

(deftest make-state-core-speccheck
  (testing "running spec.test/check"
    (let [summary (-> (stest/check `make-state-core
                                   {:clojure.spec.test.check/opts {:num-tests 10}})
                      (stest/summarize-results))]
      (is (not (contains? summary :check-failed))))))

(deftest next-state-tests
  (testing "event :ev.g.u/create"
    (is (s/valid? :g/state (next-state-core (s/conform :g/state (make-state-core))
                                                 (gen/generate gen/uuid)
                                                 {:ev/type :ev.g.u/create
                                                  :u/uuid  (gen/generate gen/uuid)}))))
  (testing "random :g/state and :ev.g.u/create event "
    (is (s/valid? :g/state (next-state-core (gen/generate (s/gen :g/state))
                                                 (gen/generate gen/uuid)
                                                 (gen/generate (s/gen :ev.g.u/create)))))))
(comment

  (run-tests)
  (all-specchecks)
  (make-state-core-speccheck)

  (list (reduce #(assoc %1 (keyword (str %2)) %2) {} (range 0 100)))

  (next-state-tests)
  (make-state-core-tests)

  ;;
  )