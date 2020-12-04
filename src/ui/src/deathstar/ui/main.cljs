(ns deathstar.ui.main
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
   [clojure.string :as str]
   [cljs.reader :refer [read-string]]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [cljctools.rsocket.spec :as rsocket.spec]
   [cljctools.rsocket.chan :as rsocket.chan]
   [cljctools.rsocket.impl :as rsocket.impl]
   [cljctools.rsocket.examples]

   [cljctools.browser-router.spec :as browser-router.spec]
   [cljctools.browser-router.chan :as browser-router.chan]
   [cljctools.browser-router.impl :as browser-router.impl]

   [deathstar.ui.spec :as ui.spec]
   [deathstar.ui.chan :as ui.chan]

   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]


   [deathstar.scenario-api.spec :as scenario-api.spec]
   [deathstar.scenario-api.chan :as scenario-api.chan]

   [deathstar.ui.render :as ui.render]))

(goog-define RSOCKET_PORT 0)

(goog-define SCENARIO_ORIGIN "")

(set! RSOCKET_PORT (str (subs js/location.port 0 2) (subs (str RSOCKET_PORT) 2)))
(set! SCENARIO_ORIGIN (format "http://localhost:%s081" (subs js/location.port 0 2)))

(def channels (merge
               (app.chan/create-channels)
               (rsocket.chan/create-channels)
               (scenario-api.chan/create-channels)
               (browser-router.chan/create-channels)
               (ui.chan/create-channels)))

(pipe (::rsocket.chan/requests| channels) (::ui.chan/ops| channels))

(pipe (::app.chan/ops| channels) (::rsocket.chan/ops| channels))
(pipe (::scenario-api.chan/ops| channels) (::rsocket.chan/ops| channels))

(def state* (ui.render/create-state*
            {::ui.spec/scenario-origin SCENARIO_ORIGIN
             ::app.spec/peer-metas {}}))

(def routes ["/" {"" ::ui.spec/page-main
                  [::app.spec/game-id ""] ::ui.spec/page-game
                  #_"/game/frequncy" #_{[::app.spec/game-id ""] ::ui.spec/page-game}}])
(def router (browser-router.impl/create-proc-ops channels state* {::browser-router.spec/routes routes}))



(defn create-proc-ops
  [channels opts]
  (let [{:keys [::ui.chan/ops|]} channels]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::ui.chan/init}
              (let [{:keys []} value]
                (println ::init)
                (ui.render/render-ui channels state* {})
                (app.chan/op
                 {::op.spec/op-key ::app.chan/request-state-update
                  ::op.spec/op-type ::op.spec/fire-and-forget}
                 channels
                 {}))

              {::op.spec/op-key ::ui.chan/update-state
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (swap! state* merge value))))

          (recur))))))

(def rsocket (rsocket.impl/create-proc-ops
              channels
              {::rsocket.spec/connection-side ::rsocket.spec/initiating
               ::rsocket.spec/host "localhost"
               ::rsocket.spec/port RSOCKET_PORT
               ::rsocket.spec/transport ::rsocket.spec/websocket}))

(def ops (create-proc-ops channels {}))

(defn ^:export main
  []
  (println ::main)
  (println ::RSOCKET_PORT RSOCKET_PORT)
  (ui.chan/op
   {::op.spec/op-key ::ui.chan/init}
   channels
   {}))


(do (main))