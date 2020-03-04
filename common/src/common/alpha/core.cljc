(ns common.alpha.core
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]))

(defn str->int [s]
  #?(:clj  (java.lang.Integer/parseInt s)
     :cljs (js/parseInt s)))

(defn rand-uuid []
  #?(:clj  (java.util.UUID/randomUUID)
     :cljs (random-uuid)))

(defn with-gen-self
  "Same as s/with-gen, but gen-fn takes  [spec] as argument.
   Caution: may be wrong, naive and design-breaking."
  [spec gen-fn]
  (let [s spec]
    (s/with-gen s (fn [] (gen-fn s)))))