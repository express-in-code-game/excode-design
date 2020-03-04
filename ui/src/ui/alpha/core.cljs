(ns ui.alpha.core
  (:require
   [clojure.repl :as repl]
   [clojure.string :as str]))

(defn deep-merge [a & maps]
  (if (map? a)
    (apply merge-with deep-merge a maps)
    (apply merge-with deep-merge maps)))



(defn idx>row
  [i w]
  (long (/ i w)))

(defn idx>col
  [i w]
  (mod i w))

(defn idx>pos
  [i w]
  (vector (idx>row i w) (idx>col i w)))