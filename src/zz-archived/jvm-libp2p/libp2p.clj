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

(defn create-opts
  [{:keys [::id]}]
  {::id id})

(def peer1-preset
  (create-opts {::id :peer1}))

(def peer2-preset
  (create-opts {::id :peer2}))

(defn start
  [{:keys [::id] :or {id :main} :as opts}]
  (go
    (let [node (-> (HostBuilder.)
                   (.protocol (into-array
                               io.libp2p.core.multistream.ProtocolBinding
                               [(Ping.)]))
                   (.listen (into-array ["/ip4/127.0.0.1/tcp/0"]))
                   (.build))]
      (-> node
          (.start)
          (.get))
      (swap! registry-ref assoc id node)
      (println ::started-node)
      (println (.listenAddresses node))
      (let []))))


(defn stop
  [{:keys [::id] :or {id :main} :as opts}]
  (go
    (let [node (get @registry-ref id)]
      (when node
        (println ::stopping-node)
        (println (.listenAddresses node))
        (-> node
            (.stop)
            (.get))
        (swap! registry-ref dissoc id)))))
