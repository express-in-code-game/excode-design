(ns starnet.alpha.common.macros
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]))

(defmacro defmethod-set
  "Iterates over a set of :ev/type keywords and calls defmethod"
  [mmethod kwset]
  `(doseq [kw# ~kwset]
     (defmethod ~mmethod kw# [x#] kw#)))

(defmacro derive-set
  "Iterates over a set of keywords and (derive k parent)"
  [tags parent]
  `(doseq [tag# ~tags]
     (derive tag# ~parent)))