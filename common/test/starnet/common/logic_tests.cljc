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


   [clojure.core.logic  :exclude [is] :as l :refer [run* == and* membero
                                                    fresh conde succeed
                                                    conso resto !=]]
   [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
   [clojure.core.logic.pldb :as pldb :refer [db with-db db-rel db-fact]]
   [clojure.core.logic.fd  :as fd]
   [clojure.core.logic.unifier :as u]))

(comment
  
  (run-tests)
  
  ;;
  )

; https://github.com/clojure/core.logic
; https://github.com/clojure/core.logic/wiki
; https://github.com/clojure/core.logic/wiki/Examples
; https://github.com/clojure/core.logic/wiki/Features
; https://github.com/clojure/core.logic/blob/master/src/test/clojure/clojure/core/logic/tests.clj
; https://github.com/clojure/core.logic/blob/master/src/test/cljs/cljs/core/logic/tests.cljs

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
      (run* [q] (nom/fresh [a b] (== a b))) '()))
  (testing "pldb examples"
    (let [_ (db-rel person p)
          _ (db-rel fruit f)
          _ (db-rel enjoys ^:index p1 ^:index f1)
          _ (db-rel  citrus f)
          _ (db-rel  sweet f)

          facts1 (db
                  [person 'A]
                  [person 'B]
                  [person 'C]

                  [fruit 'mango]
                  [fruit 'banana]
                  [fruit 'persimmon]

                  [enjoys 'A 'mango]
                  [enjoys 'A 'banana]
                  [enjoys 'B 'banana]
                  [enjoys 'C 'persimmon])

          facts2 (-> facts1
                     (db-fact citrus 'mango)
                     (db-fact sweet 'banana)
                     (db-fact sweet 'persimmon))]
      (are [x y] (= x y)
        (with-db facts2
          (run* [q]
                (fresh [x y]
                       (citrus y)
                       (enjoys x y)
                       (== q [x y])))) '([A mango])
        (with-db facts2
          (run* [q]
                (fresh [x y]
                       (sweet y)
                       (enjoys x y)
                       (== q [x y])))) '([A banana] [B banana] [C persimmon]))))
  (testing "logic.unifier/unify"
    (is (= (u/unify ['(?x ?y ?z) '(1 2 ?y)]) '(1 2 2))))
  (testing "contraint logic programming (CLP tree)"
    (are [x y] (= x y)
      (run* [q]
            (!= q 1)) '((_0 :- (!= (_0 1))))
      (run* [q]
            (fresh [x y]
                   (!= [1 x] [y 2])
                   (== q [x y]))) '(([_0 _1] :- (!= (_1 1) (_0 2))))
      )
    )
  (testing "contraint logic programming (CLP finite domains)"
    (are [x y] (= x y)
      (run* [q]
            (fd/in q (fd/interval 1 5))) '(1 2 3 4 5)
      (run* [q]
            (fresh [x y]
                   (fd/in x y (fd/interval 1 10))
                   (fd/+ x y 10)
                   (== q [x y]))) '([1 9] [2 8] [3 7] [4 6] [5 5] [6 4] [7 3] [8 2] [9 1])
      (run* [q]
            (fresh [x y]
                   (fd/in x y (fd/interval 0 9))
                   (fd/eq
                    (= (+ x y) 9)
                    (= (+ (* x 2) (* y 4)) 24))
                   (== q [x y]))) '([6 3])
      (run* [q]
            (fresh [x y]
                   (fd/in x y (fd/interval 1 10))
                   (fd/+ x y 10)
                   (== q [x y]))) '([1 9] [2 8] [3 7] [4 6] [5 5] [6 4] [7 3] [8 2] [9 1])
      (run* [q]
            (fresh [x y]
                   (fd/in x y (fd/interval 1 10))
                   (fd/+ x y 10)
                   (fd/distinct [x y])
                   (== q [x y]))) '([1 9] [2 8] [3 7] [4 6] [6 4] [7 3] [8 2] [9 1])
      ))
  )


(comment

  ;; CLP (tree)

  (run* [q]
        (!= q 1))

  (run* [q]
        (fresh [x y]
               (!= [1 x] [y 2])
               (== q [x y])))


  ;; CLP (FD) finite domains

  (run* [q]
        (fd/in q (fd/interval 1 5)))

  (run* [q]
        (fresh [x y]
               (fd/in x y (fd/interval 1 10))
               (fd/+ x y 10)
               (== q [x y])))

  (run* [q]
        (fresh [x y]
               (fd/in x y (fd/interval 0 9))
               (fd/eq
                (= (+ x y) 9)
                (= (+ (* x 2) (* y 4)) 24))
               (== q [x y])))

  (run* [q]
        (fresh [x y]
               (fd/in x y (fd/interval 1 10))
               (fd/+ x y 10)
               (fd/distinct [x y]) ;; [5 5] no longer in the set of returned solutions
               (== q [x y])))

  ;; tabling

  (defne arco [x y]
    ([:a :b])
    ([:b :a])
    ([:b :d]))

  (def patho
    (tabled [x y]
            (conde
             [(arco x y)]
             [(fresh [z]
                     (arco x z)
                     (patho z y))])))

   ;; (:b :a :d)
  (run* [q] (patho :a q))


  ;; nominal


  (defn substo [e new a out]
    (conde
     [(== ['var a] e) (== new out)]
     [(fresh [y]
             (== ['var y] e)
             (== ['var y] out)
             (nom/hash a y))]
     [(fresh [rator ratorres rand randres]
             (== ['app rator rand] e)
             (== ['app ratorres randres] out)
             (substo rator new a ratorres)
             (substo rand new a randres))]
     [(fresh [body bodyres]
             (nom/fresh [c]
                        (== ['lam (nom/tie c body)] e)
                        (== ['lam (nom/tie c bodyres)] out)
                        (nom/hash c a)
                        (nom/hash c new)
                        (substo body new a bodyres)))]))

  (run* [q]
        (nom/fresh [a b]
                   (substo ['lam (nom/tie a ['app ['var a] ['var b]])]
                           ['var b] a q)))
;; => [['lam (nom/tie 'a_0 '(app (var a_0) (var a_1)))]]

  (run* [q]
        (nom/fresh [a b]
                   (substo ['lam (nom/tie a ['var b])]
                           ['var a]
                           b
                           q)))
;; => [['lam (nom/tie 'a_0 '(var a_1))]]


  ;; definite clause grammars
  ((def-->e verb [v]
     ([[:v 'eats]] '[eats]))

   (def-->e noun [n]
     ([[:n 'bat]] '[bat])
     ([[:n 'cat]] '[cat]))

   (def-->e det [d]
     ([[:d 'the]] '[the])
     ([[:d 'a]] '[a]))

   (def-->e noun-phrase [n]
     ([[:np d n]] (det d) (noun n)))

   (def-->e verb-phrase [n]
     ([[:vp v np]] (verb v) (noun-phrase np)))

   (def-->e sentence [s]
     ([[:s np vp]] (noun-phrase np) (verb-phrase vp)))

   (run* [parse-tree]
         (sentence parse-tree '[the bat eats a cat] []))))
