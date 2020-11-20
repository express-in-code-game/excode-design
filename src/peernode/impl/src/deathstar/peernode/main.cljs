(ns deathstar.peernode.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [cljs.core.async.impl.protocols :refer [closed?]]
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

(def ^:const TOPIC-ID "deathstar-1a58070")

(comment

  (type daemon)
  (js/Object.keys daemon)
  (go
    (let [id (<p! (daemon._ipfs.id))]
      (println (js-keys id))
      (println (.-id id))
      (println (format "id is %s" id))))

  (js/Object.keys daemon._ipfs)
  (js/Object.keys daemon._ipfs.pubsub)

  (def handler (fn [msg]
                 (println (format "from: %s" msg.from))
                 (println (format "data: %s" (.toString msg.data)))
                 (println (format "topicIDs: %s" msg.topicIDs))))

  (daemon._ipfs.pubsub.subscribe
   "deathstar"
   handler)

  (daemon._ipfs.pubsub.unsubscribe
   "deathstar"
   handler)

  ; remove all handlers
  (daemon._ipfs.pubsub.unsubscribe
   "deathstar")

  (daemon._ipfs.pubsub.publish
   "deathstar"
   (-> (js/TextEncoder.)
       (.encode (str "hello " (rand-int 10)))))



  ;;
  )

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::peernode.chan/ops|
                ::peernode.chan/pubsub|
                ::peernode.chan/pubsub|m]} channels
        state-pubsubs (atom {})]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::peernode.chan/init}
              (let [{:keys []} value
                    id (.-id (<p! (daemon._ipfs.id)))]
                (println ::init)
                (daemon._ipfs.pubsub.subscribe
                 TOPIC-ID
                 (fn [msg]
                   (when-not (= id msg.from)
                     #_(do
                         #_(println (format "id: %s" id))
                         #_(println (format "from: %s" msg.from))
                         (println (format "data: %s" (.toString msg.data)))
                         #_(println (format "topicIDs: %s" msg.topicIDs)))
                     (put! pubsub| msg))))
                #_(let [counter (volatile! 0)]
                    (go (loop []
                          (<! (timeout (* 2000 (+ 1 (rand-int 2)))))
                          (vswap! counter inc)
                          (daemon._ipfs.pubsub.publish
                           TOPIC-ID
                           (-> (js/TextEncoder.)
                               (.encode (str {::some-op (str (subs id (- (count id) 7)) " " @counter)}))))
                          (recur)))))
              {::op.spec/op-key ::peernode.chan/id
               ::op.spec/op-type ::op.spec/request-response
               ::op.spec/op-orient ::op.spec/request}
              (let [{:keys [::op.spec/out|]} value
                    peerId (<p! (daemon._ipfs.id))
                    id (.-id peerId)]
                (println ::id id)
                (peernode.chan/op
                 {::op.spec/op-key ::peernode.chan/id
                  ::op.spec/op-type ::op.spec/request-response
                  ::op.spec/op-orient ::op.spec/response}
                 out|
                 {::peernode.spec/id id}))

              {::op.spec/op-key ::pubsub-sub
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys [::peernode.spec/topic-id]} value
                    pubsub| (chan (sliding-buffer 64))
                    pubsub|m (mult pubsub|)
                    id (.-id (<p! (daemon._ipfs.id)))]
                (when-not (get @state-pubsubs topic-id)
                  (swap! state-pubsubs assoc topic-id {::peernode.chan/pubsub| pubsub|
                                                       ::peernode.chan/pubsub|m pubsub|m})
                  (daemon._ipfs.pubsub.subscribe
                   topic-id
                   (fn [msg]
                     (when-not (= id (.-from msg))
                       #_(do
                           #_(println (format "id: %s" id))
                           #_(println (format "from: %s" msg.from))
                           (println (format "data: %s" (.toString msg.data)))
                           #_(println (format "topicIDs: %s" msg.topicIDs)))
                       (put! pubsub| msg))))))

              {::op.spec/op-key ::pubsub-unsub
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys [::peernode.spec/topic-id]} value
                    {:keys [::peernode.chan/pubsub|
                            ::peernode.chan/pubsub|m]} (get @state-pubsubs topic-id)]
                (when pubsub|
                  (swap! state-pubsubs dissoc topic-id)
                  (close! pubsub|)
                  (clojure.core.async/untap-all pubsub|m)
                  (daemon._ipfs.pubsub.unsubscribe topic-id)))
              
              {::op.spec/op-key ::peernode.chan/request-pubsub-stream
               ::op.spec/op-type ::op.spec/request-stream
               ::op.spec/op-orient ::op.spec/request}
              (let [{:keys [::op.spec/out| ::peernode.spec/topic-id]} value
                    {:keys [::peernode.chan/pubsub|
                            ::peernode.chan/pubsub|m]} (get @state-pubsubs topic-id)]
                #_(println ::request-pubsub-stream)
                #_(println value)
                (when pubsub|m
                  (let [pubsub|t (tap pubsub|m (chan (sliding-buffer 10)))]
                    (go (loop []
                          (when-not (or (closed? out|) (closed? pubsub|t))
                            (when-let [msg (<! pubsub|t)]
                              (peernode.chan/op
                               {::op.spec/op-key ::peernode.chan/request-pubsub-stream
                                ::op.spec/op-type ::op.spec/request-stream
                                ::op.spec/op-orient ::op.spec/response}
                               out|
                               (merge
                                {::peernode.spec/from (.-from msg)}
                                (read-string (.toString (.-data msg)))))
                              (recur))))
                        (do
                          (untap pubsub|m pubsub|t)
                          (close! pubsub|t))))))

              {::op.spec/op-key ::peernode.chan/pubsub-publish
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys [::peernode.spec/topic-id]} value]
                #_(println ::pubsub-publish)
                #_(println value)
                (daemon._ipfs.pubsub.publish
                 topic-id
                 (-> (js/TextEncoder.)
                     (.encode (str value))))))))
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