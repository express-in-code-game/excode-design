(ns deathstar.app.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [cljctools.rsocket.spec :as rsocket.spec]
   [cljctools.rsocket.chan :as rsocket.chan]
   [cljctools.rsocket.impl :as rsocket.impl]
   [cljctools.rsocket.examples]

   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]

   [cljctools.peernode.spec :as peernode.spec]
   [cljctools.peernode.chan :as peernode.chan]


   [deathstar.scenario-api.spec :as scenario-api.spec]
   [deathstar.scenario-api.chan :as scenario-api.chan]

   [deathstar.ui.spec :as ui.spec]
   [deathstar.ui.chan :as ui.chan]))

(defonce fs (js/require "fs"))
(defonce path (js/require "path"))
(defonce axios (.-default (js/require "axios")))
(defonce puppeteer (js/require "puppeteer-core"))
(defonce OrbitDB (js/require "orbit-db"))
(defonce IpfsClient (js/require "ipfs-http-client"))

(defonce channels (merge
                   (app.chan/create-channels)
                   (ui.chan/create-channels)
                   (peernode.chan/create-channels)))

(defonce channels-rsocket-peernode (rsocket.chan/create-channels))
(defonce channels-rsocket-ui (rsocket.chan/create-channels))
(defonce channels-rsocket-scenario (rsocket.chan/create-channels))
(defonce channels-rsocket-player (rsocket.chan/create-channels))

(pipe (::peernode.chan/ops| channels) (::rsocket.chan/ops| channels-rsocket-peernode))
(pipe (::rsocket.chan/requests| channels-rsocket-peernode) (::app.chan/ops| channels))
(defonce rsocket-peernode (rsocket.impl/create-proc-ops
                       channels-rsocket-peernode
                       {::rsocket.spec/connection-side ::rsocket.spec/initiating
                        ::rsocket.spec/host "peernode"
                        ::rsocket.spec/port 7000
                        ::rsocket.spec/transport ::rsocket.spec/websocket}))

(pipe (::ui.chan/ops| channels) (::rsocket.chan/ops| channels-rsocket-ui))
(go (loop []
      (when-let [value (<! (::rsocket.chan/requests| channels-rsocket-ui))]
        (let [{:keys [::op.spec/op-key]} value]
          (cond
            (isa? op-key ::app.chan/op) (put! (::app.chan/ops| channels) value)
            (isa? op-key ::scenario-api.chan/op) (put! (::rsocket.chan/ops| channels-rsocket-scenario) value)))
        (recur))))

(defonce rsocket-ui (rsocket.impl/create-proc-ops
                 channels-rsocket-ui
                 {::rsocket.spec/connection-side ::rsocket.spec/accepting
                  ::rsocket.spec/host "0.0.0.0"
                  ::rsocket.spec/port 7001
                  ::rsocket.spec/transport ::rsocket.spec/websocket}))

(pipe (::rsocket.chan/requests| channels-rsocket-player) (::rsocket.chan/ops| channels-rsocket-scenario))
(defonce rsocket-scenario (rsocket.impl/create-proc-ops
                       channels-rsocket-scenario
                       {::rsocket.spec/connection-side ::rsocket.spec/accepting
                        ::rsocket.spec/host "0.0.0.0"
                        ::rsocket.spec/port 7002
                        ::rsocket.spec/transport ::rsocket.spec/websocket}))

(go (loop []
      (when-let [value (<! (::rsocket.chan/requests| channels-rsocket-scenario))]
        (let [{:keys [::op.spec/op-key]} value]
          (cond
            (isa? op-key ::app.chan/op) (put! (::app.chan/ops| channels) value)
            :else (put! (::rsocket.chan/ops| channels-rsocket-player) value)))
        (recur))))

(defonce rsocket-player (rsocket.impl/create-proc-ops
                     channels-rsocket-player
                     {::rsocket.spec/connection-side ::rsocket.spec/accepting
                      ::rsocket.spec/host "0.0.0.0"
                      ::rsocket.spec/port 7003
                      ::rsocket.spec/transport ::rsocket.spec/websocket}))

(def state (atom
            {::app.spec/games {}}))

(def state-game-channels (atom {}))

(def ^:dynamic browser nil)
(def ^:dynamic ipfs nil)
(def ^:dynamic orbitdb nil)

(def ^:const TOPIC-ID "deathstar-1a58070")


(comment

  (js/Object.keys ipfs)
  (js/Object.keys ipfs.pubsub)

  (go
    (let [id (<p! (daemon._ipfs.id))]
      (println (js-keys id))
      (println (.-id id))
      (println (format "id is %s" id))))

  ;;
  )

(declare init-puppeteer)

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::app.chan/ops|]} channels]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::app.chan/init}
              (let [{:keys []} value]
                (println ::init)
                (try
                  (set! ipfs (IpfsClient "http://ipfs:5001"))
                  (<p! (.createInstance OrbitDB ipfs (clj->js {"directory" "/root/.orbitdb"})))
                  (catch js/Error err (println err)))
                (let [id (.-id (<p! (ipfs.id)))
                      text-decoder (js/TextDecoder.)]
                  (ipfs.pubsub.subscribe
                   TOPIC-ID
                   (fn [msg]
                     (when-not (= id (.-from msg))
                       (do
                         #_(println (format "id: %s" id))
                         (println (format "from: %s" (.-from msg)))
                         (println (format "data: %s" (.decode text-decoder  (.-data msg))))
                         #_(println (format "topicIDs: %s" msg.topicIDs)))))))
                (let [id (.-id (<p! (ipfs.id)))
                      text-encoder (js/TextEncoder.)]
                  (go (loop [counter 0]
                        (<! (timeout 3000))
                        (ipfs.pubsub.publish
                         TOPIC-ID
                         (-> text-encoder
                             (.encode  (pr-str {::id id
                                                ::counter counter}))))
                        (recur (inc counter)))))
                #_(<! (init-puppeteer))


                #_(go (loop []
                        (<! (timeout 2000))
                        #_(swap! state update ::app.spec/counter inc)
                        (ui.chan/op
                         {::op.spec/op-key ::ui.chan/update-state
                          ::op.spec/op-type ::op.spec/fire-and-forget}
                         channels
                         @state)
                        (recur)))
                #_(go
                    (let [out| (chan 64)]
                      (peernode.chan/op
                       {::op.spec/op-key ::peernode.chan/request-pubsub-stream
                        ::op.spec/op-type ::op.spec/request-stream
                        ::op.spec/op-orient ::op.spec/request}
                       channels
                       out|)
                      (loop []
                        (when-let [value  (<! out|)]
                          (println ::request-pubsub-stream)
                          (println value)
                          (recur)))))
                #_(go (loop []
                        (<! (timeout (* 1000 (+ 1 (rand-int 2)))))
                        (peernode.chan/op
                         {::op.spec/op-key ::peernode.chan/pubsub-publish
                          ::op.spec/op-type ::op.spec/fire-and-forget}
                         channels
                         {::some ::value})
                        (recur))))

              {::op.spec/op-key ::app.chan/request-state-update
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (ui.chan/op
                 {::op.spec/op-key ::ui.chan/update-state
                  ::op.spec/op-type ::op.spec/fire-and-forget}
                 channels
                 @state))

              {::op.spec/op-key ::app.chan/create-game
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [game-id (str (cljc.core/rand-uuid))
                    game {::app.spec/game-id game-id}]
                (swap! state update ::app.spec/games assoc  game-id game)
                (app.chan/op
                 {::op.spec/op-key ::app.chan/sub-to-game
                  ::op.spec/op-type ::op.spec/fire-and-forget}
                 channels
                 {::app.spec/game-id game-id})
                (ui.chan/op
                 {::op.spec/op-key ::ui.chan/update-state
                  ::op.spec/op-type ::op.spec/fire-and-forget}
                 channels
                 @state))

              {::op.spec/op-key ::app.chan/sub-to-game
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys [::app.spec/game-id]} value]
                (println ::sub-to-game)
                (when-not (get @state-game-channels game-id)
                  (let [pubsub| (chan (sliding-buffer 64))]
                    (swap! state-game-channels assoc game-id pubsub|)
                    (peernode.chan/op
                     {::op.spec/op-key ::peernode.chan/pubsub-sub
                      ::op.spec/op-type ::op.spec/fire-and-forget}
                     channels
                     {::peernode.spec/topic-id game-id})
                    (peernode.chan/op
                     {::op.spec/op-key ::peernode.chan/request-pubsub-stream
                      ::op.spec/op-type ::op.spec/request-stream
                      ::op.spec/op-orient ::op.spec/request}
                     channels
                     pubsub|
                     {::peernode.spec/topic-id game-id})
                    (go
                      (loop []
                        (when-let [msg (<! pubsub|)]
                          (println ::pubsub-msg)
                          (recur)))
                      (println (format (str ::game " process exits: % ") game-id))))))


              {::op.spec/op-key ::app.chan/unsub-from-game
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys [::op.spec/out| ::app.spec/game-id]} value]
                (println ::unsub-from-game)
                (println value)
                (let [pubsub| (get @state-game-channels game-id)]
                  (swap! state-game-channels dissoc game-id)
                  (close! pubsub|)
                  (swap! state update ::app.spec/games dissoc game-id)
                  (peernode.chan/op
                   {::op.spec/op-key ::peernode.chan/pubsub-ubsub
                    ::op.spec/op-type ::op.spec/fire-and-forget}
                   channels
                   {::peernode.spec/topic-id game-id})
                  (ui.chan/op
                   {::op.spec/op-key ::ui.chan/update-state
                    ::op.spec/op-type ::op.spec/fire-and-forget}
                   channels
                   @state)))))
          (recur))))))


(def ops (create-proc-ops channels {}))

(defn main [& args]
  (println ::main)
  (app.chan/op
   {::op.spec/op-key ::app.chan/init}
   channels
   {}))

(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))




(comment

  (cljc.core/rand-uuid)

  (go
    (println (<! (peernode.chan/op
                  {::op.spec/op-key ::peernode.chan/id
                   ::op.spec/op-type ::op.spec/request-response
                   ::op.spec/op-orient ::op.spec/request}
                  channels))))

  (go
    (let [out| (chan 64)]
      (peernode.chan/op
       {::op.spec/op-key ::peernode.chan/request-pubsub-stream
        ::op.spec/op-type ::op.spec/request-stream
        ::op.spec/op-orient ::op.spec/request}
       channels
       out|)
      (loop []
        (when-let [value  (<! out|)]
          (println value)
          (recur)))))


  (def counter (atom 0))

  (do
    (swap! counter inc)
    (peernode.chan/op
     {::op.spec/op-key ::peernode.chan/pubsub-publish
      ::op.spec/op-type ::op.spec/fire-and-forget}
     channels
     {::some @counter}))

  ;;
  )


(defn init-puppeteer
  []
  (go
    (try
      (let [data (<p! (.request axios
                                (clj->js
                                 {"url" "http://puppeteer:9222/json/version"
                                  "method" "get"
                                  "headers" {"Host" "localhost:9222"}})))

            webSocketDebuggerUrl (-> (aget (.-data data) "webSocketDebuggerUrl")
                                     (str/replace "localhost" "puppeteer"))]
        (set! browser (<p! (.connect puppeteer
                                     (clj->js
                                      {"browserWSEndpoint" webSocketDebuggerUrl
                                       #_"browserURL" #_"http://puppeteer:9222"})))))
      (catch js/Error err (println err)))))

(comment

  (go
    (let [data (<p! (.request axios
                              (clj->js
                               {"url" "http://puppeteer:9222/json/version"
                                "method" "get"
                                "headers" {"Host" "localhost:9222"}})))

          webSocketDebuggerUrl (-> (aget (.-data data) "webSocketDebuggerUrl")
                                   (str/replace "localhost" "puppeteer"))]
      (println webSocketDebuggerUrl)
      #_(println (js-keys data))
      #_(println (aget (.-data data) "webSocketDebuggerUrl"))))


  (go
    (try
      (let [page (<p! (.newPage browser))
            _ (<p! (.goto page "https://example.com"))
            dimensions (<p! (.evaluate page (fn []
                                              #js {"width" js/document.documentElement.clientWidth})))]
        (println dimensions))
      (catch js/Error err (println err))))

  ;;
  )