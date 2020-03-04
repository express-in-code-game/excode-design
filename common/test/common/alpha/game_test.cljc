(ns common.alpha.game-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :as test :refer [is run-all-tests testing deftest run-tests]]
   [common.alpha.game :refer [mk-default-game-state]]))


(deftest mk-default-game-state-tests
  (testing "generates valid :g/game"
    (is (s/valid? :g/game
                  (mk-default-game-state
                   (gen/generate gen/uuid)
                   (sgen/generate (s/gen :ev.g.u/create)))))))

(deftest all-specchecks
  (testing "running spec.test/check via stest/enumerate-namespace"
    (is (every? true?
                (map
                 #(get-in % [:clojure.spec.test.check/ret :pass?])
                 (-> (stest/enumerate-namespace 'common.alpha.game)
                     (stest/check {:clojure.spec.test.check/opts {:num-tests 10}})))))))

(deftest mk-default-game-state-speccheck
  (testing "running spec.test/check"
    (is (every? true?
                (->>
                 (stest/check `mk-default-game-state
                              {:clojure.spec.test.check/opts {:num-tests 1}})
                 (map #(get-in % [:clojure.spec.test.check/ret :pass?])))))))

(comment

  (run-tests)
  (all-specchecks)
  (mk-default-game-state-speccheck)
  
  ;;
  )