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

   [cljctools.process.spec :as process.spec]
   [cljctools.process.chan :as process.chan]
   [cljctools.process.impl :as process.impl]

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

(defonce scenario-compiler|| (process.chan/create-channels))
(defonce scenario-compiler
  (process.impl/create-proc-ops
   scenario-compiler||
   {::process.spec/process-key ::scenario-compiler
    ::process.spec/print-to-stdout? true
    ::process.spec/cmd "sh f dev"
    ::process.spec/args  #js []
    ::process.spec/child-process-options
    (clj->js {"stdio" ["pipe"]
              "shell" "/bin/bash"
              "env" (js/Object.assign
                     #js {}
                     js/global.process.env
                     #js {"SHADOWCLJS_NREPL_PORT" 8801
                          "SHADOWCLJS_HTTP_PORT" 9631
                          "RSOCKET_PORT_SCENARIO"
                          (aget js/global.process.env "RSOCKET_PORT_SCENARIO")
                          "RSOCKET_PORT_PLAYER"
                          (aget js/global.process.env "RSOCKET_PORT_PLAYER")
                          "SHADOWCLJS_DEVTOOLS_URL"
                          (aget js/global.process.env "SHADOWCLJS_DEVTOOLS_URL_SCENARIO")
                          "SHADOWCLJS_DEVTOOLS_HTTP_PORT" 9501})
              "cwd" "/ctx/DeathStarGame/bin/scenario"
              "detached" true})}))

(defonce ui-compiler|| (process.chan/create-channels))
(defonce ui-compiler
  (process.impl/create-proc-ops
   ui-compiler||
   {::process.spec/process-key ::ui-compiler
    ::process.spec/print-to-stdout? true
    ::process.spec/cmd "sh f dev"
    ::process.spec/args  #js []
    ::process.spec/child-process-options
    (clj->js {"stdio" ["pipe"]
              "shell" "/bin/bash"
              "env" (js/Object.assign
                     #js {}
                     js/global.process.env
                     #js {"SHADOWCLJS_NREPL_PORT" 8803
                          "SHADOWCLJS_HTTP_PORT" 9633
                          "SCENARIO_ORIGIN"
                          (aget js/global.process.env "SCENARIO_ORIGIN")
                          "RSOCKET_PORT"
                          (aget js/global.process.env "RSOCKET_PORT_UI")
                          "SHADOWCLJS_DEVTOOLS_URL"
                          (aget js/global.process.env "SHADOWCLJS_DEVTOOLS_URL_UI")
                          "SHADOWCLJS_DEVTOOLS_HTTP_PORT" 9503})
              "cwd" "/ctx/DeathStarGame/bin/ui"
              "detached" true})}))

(defonce peernode-compiler|| (process.chan/create-channels))
(defonce peernode-compiler
  (process.impl/create-proc-ops
   peernode-compiler||
   {::process.spec/process-key ::peernode-compiler
    ::process.spec/print-to-stdout? true
    ::process.spec/cmd "sh f dev"
    ::process.spec/args  #js []
    ::process.spec/child-process-options
    (clj->js {"stdio" ["pipe"]
              "shell" "/bin/bash"
              "env" (js/Object.assign
                     #js {}
                     js/global.process.env
                     #js {"SHADOWCLJS_NREPL_PORT" 8802
                          "SHADOWCLJS_HTTP_PORT" 9632
                          "SHADOWCLJS_DEVTOOLS_URL" "http://localhost:9632"
                          "SHADOWCLJS_DEVTOOLS_HTTP_PORT" 9502
                          "RSOCKET_PORT" 7000})
              "cwd" "/ctx/cljstools/bin/peernode"
              "detached" true})}))

(defonce peernode|| (process.chan/create-channels))
(defonce peernode
  (process.impl/create-proc-ops
   peernode||
   {::process.spec/process-key ::peernode
    ::process.spec/print-to-stdout? true
    ::process.spec/cmd "sh f daemon"
    ::process.spec/args  #js []
    ::process.spec/child-process-options
    (clj->js {"stdio" ["pipe"]
              "shell" "/bin/bash"
              "env" (js/Object.assign
                     #js {}
                     js/global.process.env
                     #js {"RSOCKET_PORT" 7000})
              "cwd" "/ctx/cljstools/bin/peernode"
              "detached" true})}))

(comment

  (process.chan/start scenario-compiler|| {})
  (process.chan/terminate scenario-compiler|| {})
  (process.chan/restart scenario-compiler|| {})
  (process.chan/print-logs scenario-compiler|| {})

  (process.chan/start ui-compiler|| {})
  (process.chan/terminate ui-compiler|| {})
  (process.chan/restart ui-compiler|| {})
  (process.chan/print-logs ui-compiler|| {})

  (js/global.process.kill 188 "SIGINT")

  ;;
  )


(def state (atom
            {::app.spec/games {}}))

(def state-game-channels (atom {}))

(def ^:dynamic browser nil)

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
                
                #_(process.chan/start scenario-compiler|| {})
                
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