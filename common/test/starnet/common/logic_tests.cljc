(ns starnet.common.logic-tests
  (:refer-clojure :exclude [==])
  (:require
   [clojure.set :refer [subset?]]
   [clojure.walk :as walk]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]

   [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
   [clojure.core.logic  :exclude [is] :as l :refer [run* == and* membero
                                                    fresh conde succeed
                                                    conso resto]]))

(comment
  
  (run-tests)
  
  ;;
  )

; https://github.com/clojure/core.logic
; https://github.com/clojure/core.logic/wiki
; https://github.com/clojure/core.logic/wiki/Examples
(deftest logic-tests
  (testing "mixed examples"
    (are [x y] (= x y)
      (run* [q] (== q true)) '(true)

      (run* [q]
            (== q {:a 1 :b 2})) '({:a 1 :b 2})
      (run* [q]
            (== {:a q :b 2} {:a 1 :b 2})) '(1)
      (run* [q]
            (== 1 q)) '(1)
      (read-string (pr-str (run* [q]
                                 (== 1 q)))) '(1)
      (run* [q]
            (== q '(1 2 3))) '((1 2 3))
      (run* [q]
            (== q 1)
            (== q 2)) '()))
  (testing "conde examples"
    (are [x y] (= x y)
      (run* [q]
            (conde
             [succeed]))  '(_0)
      (run* [q]
            (conde
             [succeed (== q 1)])) '(1)
      (run* [q]
            (conde
             [(== q 1)]
             [(== q 2)])) '(1 2)))
  (testing "conso examples"

    (are [x y] (= x y)
      (run* [q]
            (conso 1 [2 3] q)) '((1 2 3))
      (run* [q]
            (conso 1 q [1 2 3])) '((2 3))

      (run* [q]
            (conso q [2 3] [1 2 3])) '(1)
      (run* [q]
            (conso 1 [2 q] [1 2 3])) '(3)
      (run* [q]
            (resto [1 2 3 4] q)) '((2 3 4))))
  (testing "membero examples"
    (are [x y] (= x y)
      (run* [q]
            (membero 7 [1 3 8 q])) '(7)
      (run* [q] (membero q '(:cat :dog :bird))) '(:cat :dog :bird)
      (run* [q]
            (and*
             [(membero q [:t1 :t2 :t3])
              (membero q [:t2 :t3])
              (membero q [:t1 :t2])])) '(:t2)
      (let [a {:type :some-entity
               :tags #{:t1 :t2 :t3}
               :logic-alias :a}
            b {:type :some-entity
               :tags #{:t2 :t3}
               :logic-alias :a}
            c {:type :some-entity
               :tags #{:t1 :t2}
               :logic-alias :a}]
        (run* [q]
              (->> [a b c]
                   (map :tags)
                   (map vec)
                   (map (fn [xs]
                          xs
                          (membero q xs)))
                   (and*)))) '(:t2)
      (run* [q]
            (fresh [a]
                   (membero a [1 2 3])
                   (membero q [3 4 5])
                   (== a q))) '(3)))
  (testing "nominal logic examples"
    (are [x y] (= x y)
      (run* [q] (nom/fresh [a] (== a a))) '(_0)
      (run* [q] (fresh [x] (nom/fresh [a] (== a x))))  '(_0)
      (run* [q] (nom/fresh [a] (== a 5))) '()
      (run* [q] (nom/fresh [a b] (== a b))) '())))


(comment
  
  
  
  ;;
  )