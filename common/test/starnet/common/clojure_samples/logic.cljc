(ns starnet.common.clojure-samples.logic
  (:refer-clojure :exclude [==])
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.logic :as l :refer [run* == and* membero
                                     fresh conde succeed
                                     conso resto]]))

(comment

  (run* [q]
        (== q true))

  (run* [q]
        (membero q '(:cat :dog :bird :bat :zebra)))

  (run* [q]
        (and*
         [(membero q [:t1 :t2 :t3])
          (membero q [:t2 :t3])
          (membero q [:t1 :t2])]))


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
               (and*))))

  (run* [q]
        (== q {:a 1 :b 2}))

  (run* [q]
        (== {:a q :b 2} {:a 1 :b 2}))

  (type (run* [q]
              (== 1 q)))

  (read-string (pr-str (run* [q]
                             (== 1 q))))

  (run* [q]
        (== q '(1 2 3)))

  (run* [q]
        (== q 1)
        (== q 2))

  (run* [q]
        (fresh [a]
               (membero a [1 2 3])
               (membero q [3 4 5])
               (== a q)))


  (run* [q]
        (conde
         [succeed]))

  (run* [q]
        (conde
         [succeed (== q 1)]))

  (run* [q]
        (conde
         [(== q 1)]
         [(== q 2)]))

  (run* [q]
        (conso 1 [2 3] q))

  (run* [q]
        (conso 1 q [1 2 3]))

  (run* [q]
        (conso q [2 3] [1 2 3]))

  (run* [q]
        (conso 1 [2 q] [1 2 3]))

  (run* [q]
        (resto [1 2 3 4] q))

  (run* [q]
        (membero 7 [1 3 8 q]))
  
  


 ;;
  )

