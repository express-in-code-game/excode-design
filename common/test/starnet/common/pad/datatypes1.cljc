(ns starnet.common.pad.datatypes1
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
  

  ;;
  )