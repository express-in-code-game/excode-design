(ns starnet.pad.transducers1
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

(comment

  ; https://blog.cognitect.com/blog/2014/8/6/transducers-are-coming

  (def xform (comp (map inc) (filter even?)))
  (->> [1 2 3 4] (map inc) (filter even?))



  ; Once you've got a transducer, what can you do with it?

  (def data [1 2 3 4])

  ;lazily transform the data (one lazy sequence, not three as with composed sequence functions)
  (sequence xform data)

  ; reduce with a transformation (no laziness, just a loop)
  (transduce xform + 0 data)

  ; build one collection from a transformation of another, again no laziness
  (into [] xform data)

  ; create a recipe for a transformation, which can be subsequently sequenced, iterated or reduced
  (iteration xform data)

  ; or use the same transducer to transform everything that goes through a channel
  (chan 1 xform)




  (def xf (map inc))
  ((xf (constantly 1)))


  (defn inc-with-print [x]
    (println x)
    (inc x))

  (type (eduction (map inc-with-print) (map inc-with-print) (range 3)))

  ;;
  )

