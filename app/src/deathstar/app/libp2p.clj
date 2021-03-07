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
   [deathstar.spec])
  (:import
   io.libp2p.core.Host
   io.libp2p.core.dsl.HostBuilder
   io.libp2p.core.multiformats.Multiaddr
   io.libp2p.protocol.Ping
   io.libp2p.protocol.PingController))

(defonce ^:private registry-ref (atom {}))

(defn start
  [{:keys [::id] :or {id :main} :as opts}]
  (go
    (let [node (-> (HostBuilder.)
                   (.protocol (Ping.))
                   (.listen "/ip4/127.0.0.1/tcp/0")
                   (.build))]
      (-> node
          (.start)
          (.get))
      (println "libp2p node listening on:")
      (println (.listenAdresses node))
      (swap! registry-ref assoc id node)
      (let []))))


(defn stop
  [{:keys [::id] :or {id :main} :as opts}]
  (go
    (let [node (get @registry-ref id)]
      (when node
        (-> node
            (.stop)
            (.get))))))
