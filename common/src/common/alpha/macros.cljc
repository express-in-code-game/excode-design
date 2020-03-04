(ns common.alpha.macros
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]))

(defmacro defmethods-for-a-set
  "Iterates over a #{} of :ev/type keywords and calls defmethod"
  [mmethod kwset]
  `(doseq [kw# ~kwset]
     (defmethod ~mmethod kw# [x#] kw#)))
