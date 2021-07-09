(ns project.core
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]))


(defn operation-fn
  [ops| & {:keys []}]
  (fn f
    ([op] (f op {}))
    ([op opts]
     (go
       (if-not (clojure.core.async.impl.protocols/closed? ops|)
         (let [out| (chan 1)]
           (put! ops| {:op op :opts opts :out| out|})
           (if (:timeout opts)
             (alt!
               out| ([v] v)
               (timeout (:timeout opts)) nil)
             (<! out|)))
         false)))))