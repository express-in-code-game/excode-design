(ns deathstar.scenario.rovers1.player
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.set :refer [subset?]]

   [deathstar.scenario.rovers1.game-api :as api]))


(comment

  (api/move 3 4)
  
  ;;
  )