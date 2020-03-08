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
; https://github.com/gigasquid/chemical-computing

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


(defn reactf
  [f x]
  (cond
    (= 1 (count (:els f)))
    (->>
     (apply (:reactf f) (into (:els f) [x]))
     (into [(assoc f :els [])]))
    :else [(update-in f [:els] conj x)]))

(defn reaction-1
  [[x1 x2]]
  (cond
    (and (:reactf x1) (:reactf x2)) [x1 x2]
    (:reactf x1) (reactf x1 x2)
    (:reactf x2) (reactf x2 x1)
    :else [x1 x2]))

(comment


  (def mostly-ints (gen/frequency [[9 gen/small-integer] [1 (gen/return nil)]]))
  (->> (gen/sample mostly-ints 10000) (filter nil?) (count))

  (def gen-element1 (gen/fmap #(identity {:val %}) (gen/choose 2 35)))
  (def gen-element2 (gen/fmap #(identity {:val %}) (gen/choose 36 70)))
  (def gen-element3 (gen/fmap #(identity {:val %}) (gen/choose 71 101)))
  (def gen-elementfn (gen/return {:els []
                                  :reactf (fn rf1 [x1 x2]
                                            (let [a (:val x1)
                                                  b (:val x2)]
                                              (if (and (some? a)
                                                       (some? b)
                                                       (> a b)
                                                       (zero? (mod a b)))
                                                [{:val b}]
                                                [{:val a} {:val b}])))}))

  (gen/generate gen-element1)
  (def f1 (gen/generate gen-elementfn))
  ((:reactf f1) {:val 6} {:val 5})

  (def elgen1 (gen/frequency [[1 gen-element1]
                              [2 gen-element2]
                              [2 gen-element3]
                              [5 gen-elementfn]]))
  (def mols (gen/sample elgen1 100))

  (->>
   (reaction-cycle 1000 mix-and-react reaction-1 mols)
   (filter #(some? (:val %)))
   (into  (sorted-set-by #(compare (:val %1) (:val %2))))
   (map :val))

  ;;
  )