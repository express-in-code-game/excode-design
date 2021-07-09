(ns deathstar.scenario.player.main
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

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [cljctools.rsocket.spec :as rsocket.spec]
   [cljctools.rsocket.chan :as rsocket.chan]
   [cljctools.rsocket.impl :as rsocket.impl]

   [deathstar.scenario.spec :as scenario.spec]
   [deathstar.scenario.chan :as scenario.chan]
   [deathstar.scenario.core :as scenario.core]

   [deathstar.scenario.player.spec :as player.spec]
   [deathstar.scenario.player.chan :as player.chan]
   [deathstar.scenario.player.impl :as player.impl]

   [deathstar.scenario.player.main.spec :as player.main.spec]
   [deathstar.scenario.player.main.chan :as player.main.chan]))

(goog-define RSOCKET_PORT 0)

(set! RSOCKET_PORT (str (subs js/location.port 0 2) (subs (str RSOCKET_PORT) 2)))

(def channels (merge
               (scenario.chan/create-channels)
               (player.main.chan/create-channels)
               (player.chan/create-channels)
               (rsocket.chan/create-channels)))

(pipe (::rsocket.chan/requests| channels) (::player.chan/ops| channels))

(pipe (::scenario.chan/ops| channels) (::rsocket.chan/ops| channels))

(def state (atom {}))

(comment

  (swap! state assoc :random (rand-int 10))

  (scenario.chan/op
   {::op.spec/op-key ::scenario.chan/move-rovers
    ::op.spec/op-type ::op.spec/fire-and-forget}
   channels
   {::scenario.spec/x (rand-int scenario.spec/x-size)
    ::scenario.spec/y (rand-int scenario.spec/y-size)})

  ;;
  )

(defn create-proc-ops
  [channels opts]
  (let [{:keys [::player.main.chan/ops|]} channels

        player-exit|* (atom nil)

        start-proc-player
        (fn []
          (go
            (let [exit| @player-exit|*]
              (when (and exit| (not (closed? exit|)))
                (let [out| (chan 1)]
                  (put! exit| {::op.spec/out| out|})
                  (<! out|)
                  (close! exit|))))
            (let [exit| (chan 1)]
              (reset! player-exit|* exit|)
              (player.impl/create-proc-ops (merge
                                            channels
                                            {::player.chan/exit| exit|})
                                           {}))))]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::player.main.chan/init}
              (let [{:keys []} value]
                (println ::init)
                (<! (start-proc-player))

                #_(go (loop []
                        (<! (timeout 3000))
                        (scenario.chan/op
                         {::op.spec/op-key ::scenario.chan/move-rovers
                          ::op.spec/op-type ::op.spec/fire-and-forget}
                         channels
                         {:random (rand-int 100)})
                        (recur)))))))
        (recur)))))

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
  (player.main.chan/op
   {::op.spec/op-key ::player.main.chan/init}
   channels
   {}))


(do (main))