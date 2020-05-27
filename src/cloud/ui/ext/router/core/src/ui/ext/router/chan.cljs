(ns ui.ext.router.chan
  (:require
   [goog.string :as gstring]
   [goog.string.format]))

(def ^:const OP :op)

(defn op [v] (get v OP))

(defn op-route [] :router/pushed)
(defn vl-route [url handler] {OP (op-route) :url url :handler handler})

