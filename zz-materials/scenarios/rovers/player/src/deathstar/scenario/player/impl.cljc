(ns deathstar.scenario.player.impl
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

   [deathstar.scenario.spec :as scenario.spec]
   [deathstar.scenario.chan :as scenario.chan]
   [deathstar.scenario.core :as scenario.core]

   [deathstar.scenario.player.spec :as player.spec]
   [deathstar.scenario.player.chan :as player.chan]))

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
  (let [{:keys [::player.chan/ops|
                ::player.chan/exit|]} channels]
    (go
      (loop []
        (when-let [[value port] (alts! [ops| exit|])]
          (condp = port

            exit|
            (let [{:keys [::op.spec/out|]} value]
              (close! out|))

            ops|
            (do
              (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])


                {::op.spec/op-key ::player.chan/next-move
                 ::op.spec/op-type ::op.spec/request-response
                 ::op.spec/op-orient ::op.spec/request}
                (let [{:keys [::op.spec/out|
                              ::scenario.spec/step]} value
                      ops
                      [{::op.spec/op-key ::scenario.chan/move-rovers
                        ::op.spec/op-type ::op.spec/fire-and-forget
                        ::scenario.spec/choose-location ::scenario.spec/closest
                        ::scenario.spec/location-type ::scenario.spec/signal-tower}
                       {::op.spec/op-key ::scenario.chan/move-rovers
                        ::op.spec/op-type ::op.spec/fire-and-forget
                        ::scenario.spec/choose-location ::scenario.spec/closest
                        ::scenario.spec/location-type ::scenario.spec/recharge}
                       {::op.spec/op-key ::scenario.chan/move-rovers
                        ::op.spec/op-type ::op.spec/fire-and-forget
                        ::scenario.spec/choose-location ::scenario.spec/closest
                        ::scenario.spec/location-type ::scenario.spec/signal-tower}
                       {::op.spec/op-key ::scenario.chan/scan
                        ::op.spec/op-type ::op.spec/fire-and-forget
                        ::scenario.spec/energy-percentage 0.3}
                       {::op.spec/op-key ::scenario.chan/move-rovers
                        ::op.spec/op-type ::op.spec/fire-and-forget
                        ::scenario.spec/choose-location ::scenario.spec/closest
                        ::scenario.spec/location-type ::scenario.spec/signal-tower}
                       {::op.spec/op-key ::scenario.chan/move-rovers
                        ::op.spec/op-type ::op.spec/fire-and-forget
                        ::scenario.spec/choose-location ::scenario.spec/closest
                        ::scenario.spec/location-type ::scenario.spec/signal-tower}
                       {::op.spec/op-key ::scenario.chan/move-rovers
                        ::op.spec/op-type ::op.spec/fire-and-forget
                        ::scenario.spec/choose-location ::scenario.spec/closest
                        ::scenario.spec/location-type ::scenario.spec/recharge}
                       {::op.spec/op-key ::scenario.chan/move-rovers
                        ::op.spec/op-type ::op.spec/fire-and-forget
                        ::scenario.spec/choose-location ::scenario.spec/closest
                        ::scenario.spec/location-type ::scenario.spec/signal-tower}
                       {::op.spec/op-key ::scenario.chan/move-rovers
                        ::op.spec/op-type ::op.spec/fire-and-forget
                        ::scenario.spec/choose-location ::scenario.spec/closest
                        ::scenario.spec/location-type ::scenario.spec/recharge}
                       {::op.spec/op-key ::scenario.chan/move-rovers
                        ::op.spec/op-type ::op.spec/fire-and-forget
                        ::scenario.spec/choose-location ::scenario.spec/closest
                        ::scenario.spec/location-type ::scenario.spec/signal-tower}]]
                  (player.chan/op
                   {::op.spec/op-key ::player.chan/next-move
                    ::op.spec/op-type ::op.spec/request-response
                    ::op.spec/op-orient ::op.spec/response}
                   out|
                   {::scenario.chan/op  (get ops step)})))
              (recur))))))))