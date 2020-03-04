(ns common.alpha.game-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
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
                 (map #(get-in % [:clojure.spec.test.check/ret :pass?])))
                ))))

(comment

  (run-tests)
  (all-specchecks)
  (mk-default-game-state-speccheck)

  (def x (stest/check `mk-default-game-state
                      {:clojure.spec.test.check/opts {:num-tests 10}}))
  (def r (first x))
  (type r)
  (keys r)
  (keys (:clojure.spec.test.check/ret r))
  (keys (get-in r [:clojure.spec.test.check/ret]))
  (keys (ex-data (get-in r [:clojure.spec.test.check/ret :result])))
  (def ex (ex-data (get-in r [:clojure.spec.test.check/ret :result])))
  (dissoc ex :clojure.spec.alpha/value :clojure.spec.test.alpha/val)
  (select-keys [:clojure.spec.alpha/problems :clojure.spec.alpha/spec])

  (def r1 (-> r
              (update-in [:clojure.spec.test.check/ret :result] ex-data)
              (update-in [:clojure.spec.test.check/ret :result]
                         dissoc :clojure.spec.alpha/value :clojure.spec.test.alpha/val)
              (get-in [:clojure.spec.test.check/ret :result])))
  (def r2 (-> r
              (update-in [:clojure.spec.test.check/ret :result] ex-data)
              (update-in [:clojure.spec.test.check/ret :result]
                         dissoc :clojure.spec.alpha/value :clojure.spec.test.alpha/val)
              (update-in [:failure] ex-data)
              (update-in [:failure]
                         dissoc :clojure.spec.alpha/value :clojure.spec.test.alpha/val)
              (update-in [:shrunk :result] ex-data)
              (update-in [:shrunk :result]
                         dissoc :clojure.spec.alpha/value :clojure.spec.test.alpha/val)
              
              ))
  (keys r)
  (keys (get-in r [:clojure.spec.test.check/ret :shrunk]))
  (:spec r)
  (:sym r)
  (:failure r)
  (keys (ex-data (:failure r)))

  (-> r
      (update-in [:clojure.spec.test.check/ret :result] ex-data)
      (update-in [:clojure.spec.test.check/ret :result]
                 dissoc :clojure.spec.alpha/value :clojure.spec.test.alpha/val))



  ;;
  )