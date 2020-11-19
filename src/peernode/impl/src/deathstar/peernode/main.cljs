(ns deathstar.peernode.main
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
   [cljs.nodejs :as node]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [cljctools.rsocket.spec :as rsocket.spec]
   [cljctools.rsocket.chan :as rsocket.chan]
   [cljctools.rsocket.impl :as rsocket.impl]
   [cljctools.rsocket.examples-nodejs]
   [cljctools.rsocket.examples]

   [deathstar.peernode.spec :as peernode.spec]
   [deathstar.peernode.chan :as peernode.chan]))

(def fs (node/require "fs"))
(def path (node/require "path"))

(def channels (merge
               (rsocket.chan/create-channels)
               (peernode.chan/create-channels)))

(pipe (::rsocket.chan/requests| channels) (::peernode.chan/ops| channels))

(def ^:dynamic daemon nil)

(comment

  (type daemon)
  (js/Object.keys daemon)
  (go
    (let [id (<p! (daemon._ipfs.id))]
      (println (js-keys id))
      (println (.-id id))
      (println (format "id is %s" id))))

  ;;
  )

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::peernode.chan/ops|]} channels]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::peernode.chan/init}
              (let [{:keys []} value]
                (println ::init))

              {::op.spec/op-key ::peernode.chan/id
               ::op.spec/op-type ::op.spec/request-response
               ::op.spec/op-orient ::op.spec/request}
              (let #_[{:keys [::op.spec/out|]} value
                      peerId (<p! (daemon._ipfs.id))
                      id (.-id peerId)]
                [{:keys [::op.spec/out|]} value
                 id "hello"]
                (println ::id id)
                (peernode.chan/op
                 {::op.spec/op-key ::peernode.chan/id
                  ::op.spec/op-type ::op.spec/request-response
                  ::op.spec/op-orient ::op.spec/response}
                 out|
                 {::peernode.spec/id id}))
              
              {::op.spec/op-key ::peernode.chan/request-pubsub-stream
               ::op.spec/op-type ::op.spec/request-stream
               ::op.spec/op-orient ::op.spec/request}
              (let [{:keys [::op.spec/out|]} value]
                (println ::request-pubsub-stream)
                (println value)
                (go (loop []
                      (let [random (+ 1 (rand-int 2))]
                        (<! (timeout (* 1000 random)))
                        (peernode.chan/op
                         {::op.spec/op-key ::peernode.chan/request-pubsub-stream
                          ::op.spec/op-type ::op.spec/request-stream
                          ::op.spec/op-orient ::op.spec/response}
                         out|
                         {::peernode.spec/id random}))
                      (recur))))
              
              
              )))
        (recur)))))

(def rsocket (rsocket.impl/create-proc-ops
              channels
              {::rsocket.spec/connection-side ::rsocket.spec/accepting
               ::rsocket.spec/host "0.0.0.0"
               ::rsocket.spec/port 7000
               ::rsocket.spec/transport ::rsocket.spec/websocket}))

(def peernode (create-proc-ops channels {}))

(defn main [d]
  (println ::main)
  (println (js-keys d._ipfs))
  (set! daemon d)
  (peernode.chan/op
   {::op.spec/op-key ::peernode.chan/init}
   channels
   {::daemon d}))

(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))