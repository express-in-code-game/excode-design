(ns starnet.common.pad.chem1
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

   [clojure.core.logic.nominal :as nom]
   [clojure.core.logic :as l]
   [clojure.core.logic.protocols :as lprot]
   [clojure.core.logic.pldb :as pldb]
   [clojure.core.logic.fd  :as fd]
   [clojure.core.logic.unifier :as u]
   [clojure.core.logic.arithmetic :as la]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]])
  #?(:clj (:import clojure.core.logic.fd.IntervalFD)))

; http://gigasquidsoftware.com/chemical-computing/index.html

(defn prime?
  [n]
  (let [possible-factors (range 2 n)
        remainders (map #(mod n %) possible-factors)]
    (not (some zero? remainders))))

(defn gen-primes
  [n]
  (filter prime? (range 2 (inc n))))

(comment

  (prime? 5)

  (prime? 6)

  (gen-primes 100)
  
  ;;
  )

(defn prime-reaction
  [[a b]]
  (if (and (some? a)
           (some? b)
           (> a b)
           (zero? (mod a b)))
    [(/ a b) b]
    [a b]))

(comment 
  
  (prime-reaction [6 2])
  (prime-reaction [5 2])
  
  ;;
  )

(def molecules (range 2 101))
(defn mix-and-react
  ([mols]
   (mix-and-react prime-reaction mols))
  ([f mols]
   (let [mixed (partition 2 2 [nil] (shuffle mols))
         reacted (map f mixed)]
     (->> reacted
          (flatten)
          (filter some?)))))

(defn reaction-cycle
  ([n mols]
   (reaction-cycle n mix-and-react prime-reaction  mols))
  ([n f-mix f-react mols]
   (if (zero? n)
     mols
     (recur (dec n) f-mix f-react (f-mix f-react mols)))))

(comment

  (take 10 (mix-and-react molecules))

  (take 10 (reaction-cycle 100 mix-and-react prime-reaction molecules))

  (let [xs (reaction-cycle 1000 mix-and-react prime-reaction molecules)]
    (-> xs (distinct) (sort)))

  ;;
  )

(defn prime-reaction-reducing
  [[a b]]
  (if (and (some? a)
           (some? b)
           (> a b)
           (zero? (mod a b)))
    [b]
    [a b]))

(comment

  (->>
   (reaction-cycle 1000 mix-and-react prime-reaction-reducing molecules)
   #_'no-distinct
   (sort))

  ;;
  )