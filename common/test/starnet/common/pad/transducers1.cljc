(ns starnet.common.pad.transducers1
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.reducers :as r]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]))



(comment
  
  ; https://vimeo.com/45561411

  (def v (into [] (range 10000000)))
  (time (reduce + (map inc (filter even? v))))
  (time (reduce + (r/map inc (r/filter even? v))))
  (time (r/fold + (r/map inc (r/filter even? v))))


  ;;
  )