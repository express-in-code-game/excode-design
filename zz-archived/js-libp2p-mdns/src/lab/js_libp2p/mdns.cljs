(ns lab.js-libp2p.mdns
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   [cljs.nodejs :as node]))

(def fs (node/require "fs"))
(def path (node/require "path"))
(def Libp2p (node/require "libp2p"))
(def TCP (node/require "libp2p-tcp"))
(def Mplex (node/require "libp2p-mplex"))
(def NOISE (.-NOISE (node/require "libp2p-noise")))
(def MulticastDNS (node/require "libp2p-mdns"))

(defn create-node
  []
  (.create Libp2p
           (clj->js {"addresses" {"listen" ["/ip4/0.0.0.0/tcp/0"]}
                     "modules" {"transport" [TCP]
                                "streamMuxer" [Mplex]
                                "connEncryption" [NOISE]
                                "peerDiscovery" [MulticastDNS]}
                     "config" {"peerDiscovery" {MulticastDNS.tag {"interval" 20000
                                                                  "enabled" true}}}})))

(def ^:dynamic node* nil)

(comment

  (js/Object.keys node*)
  (aget node* "peerId")
  (.. node* -peerId (toB58String))

  ;;
  )

(defn main []
  (println ::main)
  (println "hello lab")
  (println (type Libp2p))
  (println (type MulticastDNS))
  (println (type NOISE))
  (println (type Mplex))
  (println (type TCP))
  (println (type MulticastDNS.tag))
  (go
    (let [node (<p! (create-node))]
      (set! node* node)
      (.on node "peer:discovery" (fn [peer-id]
                                   (println (format "discovered %s" (.toB58String peer-id)))))
      (.start node))))

#_(do (main))