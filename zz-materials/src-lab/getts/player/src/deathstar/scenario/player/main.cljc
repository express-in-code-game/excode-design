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
   [deathstar.scenario.player.chan :as player.chan]))

(goog-define RSOCKET_PORT 0)

(def channels (merge
               (scenario.chan/create-channels)
               (player.chan/create-channels)
               (rsocket.chan/create-channels)))

(pipe (::rsocket.chan/requests| channels) (::player.chan/ops| channels))

(pipe (::scenario.chan/ops| channels) (::rsocket.chan/ops| channels))

(def state (atom {}))

(comment

  (swap! state assoc :random (rand-int 10))

  (scenario.chan/op
   {::op.spec/op-key ::scenario.chan/move-rover
    ::op.spec/op-type ::op.spec/fire-and-forget}
   channels
   {::scenario.core/x (rand-int scenario.core/x-size)
    ::scenario.core/y (rand-int scenario.core/y-size)})

  ;;
  )

(defn create-proc-ops
  [channels opts]
  (let [{:keys [::player.chan/ops|]} channels]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::player.chan/init}
              (let [{:keys []} value]
                (println ::init)
                #_(go (loop []
                        (<! (timeout 3000))
                        (scenario.chan/op
                         {::op.spec/op-key ::scenario.chan/move-rover
                          ::op.spec/op-type ::op.spec/fire-and-forget}
                         channels
                         {:random (rand-int 100)})
                        (recur))))

              {::op.spec/op-key ::player.chan/next-move
               ::op.spec/op-type ::op.spec/request-response
               ::op.spec/op-orient ::op.spec/request}
              (let [{:keys [::op.spec/out|
                            ::scenario.core/entities-in-range
                            ::scenario.core/rover]} value
                    location (second
                              (first (filter
                                      (fn [[k entity]]
                                        (and
                                         (= (::scenario.core/entity-type entity)
                                            ::scenario.core/location)
                                         (not= (select-keys
                                                rover
                                                [::scenario.core/x ::scenario.core/y])
                                               (select-keys
                                                entity
                                                [::scenario.core/x ::scenario.core/y])))) entities-in-range)))]
                (player.chan/op
                 {::op.spec/op-key ::player.chan/next-move
                  ::op.spec/op-type ::op.spec/request-response
                  ::op.spec/op-orient ::op.spec/response}
                 out|
                 (select-keys location [::scenario.core/x ::scenario.core/y])
                 #_{::scenario.core/x (rand-int scenario.core/x-size)
                    ::scenario.core/y (rand-int scenario.core/y-size)})))))
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
  (player.chan/op
   {::op.spec/op-key ::player.chan/init}
   channels
   {}))


(do (main))