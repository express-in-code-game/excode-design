(ns deathstar.multiplayer.hub.store.mem.api
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [deathstar.multiplayer.protocols :as p]
   [deathstar.multiplayer.spec :as spec]
   [cljctools.cljc.core :as cljc.core]))


(defn create-state
  [data]
  (atom data))

(defn create-default-state-data
  []
  {::spec/users {}
   ::spec/games {}})