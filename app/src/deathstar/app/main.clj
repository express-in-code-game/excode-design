(ns deathstar.app.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [deathstar.app.spec :as app.spec]
   [deathstar.app.libp2p :as app.libp2p]))

(defn -main [& args]
  (println ::-main)
  (let [system-exit| (chan 1)]

    (go
      (<! system-exit|)
      (println ::exiting)
      (System/exit 0))))
