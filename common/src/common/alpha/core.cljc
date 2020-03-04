(ns common.alpha.core
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]))

(defn str->int [s]
  #?(:clj  (java.lang.Integer/parseInt s)
     :cljs (js/parseInt s)))

(defn rand-uuid []
  #?(:clj  (java.util.UUID/randomUUID)
     :cljs (random-uuid)))