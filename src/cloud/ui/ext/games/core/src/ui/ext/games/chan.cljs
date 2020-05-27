(ns ui.ext.games.chan
  (:require
   [goog.string :as gstring]
   [goog.string.format]))

(def ^:const OP :op)

(defn op [v] (get v OP))

(defn op-render [] :op/render)
(defn vl-render [] {OP (op-render)})