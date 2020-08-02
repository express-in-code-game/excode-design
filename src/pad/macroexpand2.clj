(ns pad.macroexpand2
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [clojure.walk]
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))

;; https://github.com/clojure/clojure/blob/clojure-1.10.1/src/clj/clojure/core.clj#L2776
(defmacro declare2
  "defs the supplied var names with no bindings, useful for making forward declarations."
  {:added "1.0"}
  [& names] `(do ~@(map #(list 'def (vary-meta % assoc :declared true)) names)))

(s/fdef declare2
  :args (s/cat :names (s/* #(= (count (str %)) 3)))
  :ret any?)

;; should throw, but does not
(defn f1
  []
  (go
    #_(let () :foo)
    (declare2 foo abcd)))

;; throws, as expected
#_(defn f2
  []
  (fn []
    (prn 3)
    (map (fn []
           (let []
             (declare2 foo abcd))) [])))

(comment

  (clojure.walk/macroexpand-all '(go (declare2 foo abcd)))

  (clojure.walk/macroexpand-all '(declare2 foo abcd))

  (clojure.walk/macroexpand-all '(let () :foo))

  (clojure.walk/macroexpand-all '(go (let () :foo)))

  (clojure.walk/macroexpand-all '(go (for (x (range 3)) x)))


  (macroexpand '(when-let [x 3] x))

  (clojure.walk/macroexpand-all '(go
                                   (declare2 foo abcd)
                                   (<! (chan 1))
                                   (let ())))



  ;;
  )


(def opkeys #{:o1 :o2})

(defmacro assert-op
  [chkey opkey]
  `(do
     (prn ~opkey)
     (prn ~opkeys)
     (prn ~(type opkeys))
     (prn ~(into [] opkeys))
     (prn ~(vector  opkey))
     (prn (opkeys ~opkey))
     (when-not (opkeys ~opkey)
       (throw (Exception. "no such op")))))

(defmacro op
  [chkey opkey]
  (assert-op chkey opkey)
  `~opkey)


(comment

  (macroexpand '(op :c1 :o1))
  
  (macroexpand '(op :c1 :o21))
  
  (macroexpand '(go
                  (op :c1 :o21)))

  (macroexpand '(go (loop []
                      (op :c1 :o21))))

  ;;
  )

(s/def ::opkeys #{:o1 :o2})

(s/def ::chkey keyword?)

(s/def ::opkey ::opkeys)

(s/def ::args (s/cat :chkey ::chkey
                     :opkey ::opkey))

(do (clojure.spec.alpha/check-asserts true))

#_clojure.spec.alpha/*compile-asserts*
#_(set! clojure.spec.alpha/*compile-asserts* false)
#_(alter-var-root #'clojure.spec.alpha/*compile-asserts* (constantly false))
#_(clojure.spec.alpha/check-asserts?)

#_(gen/generate (s/gen ::opkey))
#_(s/valid? ::opkey :o21)
#_(s/assert ::opkey :o21)

(defmacro assert-op2
  [chkey opkey]
  `(do
     (s/assert ::chkey  ~chkey)
     (s/assert ::opkey  ~opkey)
     #_(when-not (opkeys ~opkey)
         (throw (Exception. "no such op")))))

(defmacro op2
  [chkey opkey]
  (assert-op2 chkey opkey)
  `~opkey)

(defn tmp1 []
  (go
    (<! (chan 1))
    (op2 :c1 :o21)))

(comment

  (macroexpand '(op2 :c1 :o1))

  (macroexpand '(op2 :c1 :o21))

  (macroexpand '(go
                  (op2 :c1 :o1)))
  (macroexpand '(go
                  (op2 :c1 :o21)))

  (macroexpand '(go (loop []
                      (op2 :c1 :o21))))

  ;;
  )