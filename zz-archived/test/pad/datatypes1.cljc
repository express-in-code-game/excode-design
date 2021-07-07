(ns starnet.pad.datatypes1
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]))

(defn foo []
  "foo")

(comment

  ; https://clojure.org/reference/datatypes
  ; https://clojure.org/reference/protocols

  (defprotocol P
    (foo [x])
    (bar-me [x] [x y]))

  (deftype Foo [a b c]
    P
    (foo [x] a)
    (bar-me [x] b)
    (bar-me [x y] (+ c y)))

  (bar-me (Foo. 1 2 3) 42)

  (foo
   (let [x 42]
     (reify P
       (foo [this] 17)
       (bar-me [this] x)
       (bar-me [this y] x))))
  


  (defprotocol P1
    (one [this x] "method one")
    (two [this x y] "method two"))

  (defprotocol P2
    (foo [this x] "method foo")
    (bar [this x y] "method bar"))

  (deftype T1 [^{:volatile-mutable true} a b]
    P1
    (one [_ x] (set! a x))
    (two [_ x y] [x y])
    P2
    (foo [_ x] [a b])
    (bar [_ x y] (list x y)))

  (def t1 (T1. 1 2))
  (.foo t1 4)
  (.one t1 5)

  (def t2 (->T1 3 4))
  (.two t2 4 4)

  (def t3 (read-string "#starnet.pad.datatypes1.T1[5 5]"))
  (.two t3 4 4)

  (defrecord R1 [a b]
    P1
    (one [this x] (:a this))
    (two [_ x y] [x y])
    P2
    (foo [this x] (:b this))
    (bar [_ x y] (list x y)))

  (def r1 (R1. 3 4))
  (.foo r1 "")
  (type (assoc r1 :c 5))

  (def r2 (read-string "#starnet.pad.datatypes1.R1[7 8]"))
  (.bar r2 1 1)

  (map->R1 {:a 8 :b nil})

  ;;
  )


(comment 
  
  
  
  
  ;;
  )