(ns deathstar.app.libp2p
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io]

   [byte-streams]
   [aleph.http]
   [jsonista.core :as j]
   [deathstar.spec]
   [cljctools.libp2p.core]))

(defonce ^:private registry-ref (atom {}))

(defn create-opts
  [{:keys [::id]}]
  {::id id})

(def peer1-preset
  (create-opts {::id :peer1}))

(def peer2-preset
  (create-opts {::id :peer2}))

(defn start
  [{:keys [::id] :or {id :main} :as opts}]
  (go))


(defn stop
  [{:keys [::id] :or {id :main} :as opts}]
  (go))
