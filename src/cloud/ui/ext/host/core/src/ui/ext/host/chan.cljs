(ns ui.ext.host.chan
  (:require
   [goog.string :as gstring]
   [goog.string.format]))

(def ^:const OP :op)

(defn op [v] (get v OP))

(defn op-register [] :host/register)
(defn vl-register [k ext] {OP (op-register) :k k :ext ext})

(defn op-unregister [] :host/unregister)
(defn vl-unregister [k ext] {OP (op-unregister) :k k :ext ext})
