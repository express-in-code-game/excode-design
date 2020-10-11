(ns deathstar.extension.http-chan.chan
  #?(:cljs (:require-macros [deathstar.extension.http-chan.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]))



(defn create-channels
  []
  (let [request| (chan 10)]
    {::request| request|}))