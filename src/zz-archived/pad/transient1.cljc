(ns starnet.common.pad.transient1
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.reducers :as r]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]))


(comment
  
  ; https://clojure.org/reference/transients

  (defn vrange [n]
    (loop [i 0 v []]
      (if (< i n)
        (recur (inc i) (conj v i))
        v)))

  (defn vrange2 [n]
    (loop [i 0 v (transient [])]
      (if (< i n)
        (recur (inc i) (conj! v i))
        (persistent! v))))

  ;; benchmarked (Java 1.8, Clojure 1.7)
  (time (count (vrange 1000000)))    ;; 73.7 ms
  (time (count (vrange2 1000000)))   ;; 19.7 ms


  ;;
  )