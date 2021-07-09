(ns deathstar.scenario.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   #?(:cljs [cljs.core.async.interop :refer-macros [<p!]])
   #?(:cljs [goog.string.format])
   #?(:cljs [goog.string :refer [format]])
   #?(:cljs [goog.object])
   #?(:cljs [cljs.reader :refer [read-string]])

   [clojure.string :as string]
   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [cljctools.rsocket.spec :as rsocket.spec]
   [cljctools.rsocket.chan :as rsocket.chan]
   [cljctools.rsocket.impl :as rsocket.impl]

   [deathstar.scenario-api.chan :as scenario-api.chan]
   [deathstar.scenario-api.spec :as scenario-api.spec]

   [deathstar.scenario.player.spec :as player.spec]
   [deathstar.scenario.player.chan :as player.chan]

   [deathstar.scenario.spec :as scenario.spec]
   [deathstar.scenario.chan :as scenario.chan]
   [deathstar.scenario.impl :as scenario.impl]

   [deathstar.scenario.main.spec :as scenario.main.spec]
   [deathstar.scenario.main.chan :as scenario.main.chan]

   [deathstar.scenario.core :as scenario.core]
   [deathstar.scenario.render :as scenario.render]))

(goog-define RSOCKET_PORT 0)

(set! RSOCKET_PORT (str (subs js/location.port 0 2) (subs (str RSOCKET_PORT) 2)))

(def channels (merge
               (scenario.main.chan/create-channels)
               (scenario-api.chan/create-channels)
               (scenario.chan/create-channels)
               (player.chan/create-channels)
               (rsocket.chan/create-channels)))

(pipe (::rsocket.chan/requests| channels) (::scenario.main.chan/ops| channels))

#_(pipe (::scenario-api.chan/ops| channels) (::rsocket.chan/ops| channels))
(pipe (::scenario-api.chan/ops| channels) (::scenario.main.chan/ops| channels))
(pipe (::player.chan/ops| channels) (::rsocket.chan/ops| channels))

(defonce state* (scenario.render/create-state* {}))

(comment

  (swap! state* assoc :random (rand-int 10))

  (scenario.chan/op
   {::op.spec/op-key ::scenario.chan/move-rovers
    ::op.spec/op-type ::op.spec/fire-and-forget}
   channels
   {::scenario.spec/x (rand-int scenario.spec/x-size)
    ::scenario.spec/y (rand-int scenario.spec/y-size)})


  (scenario.chan/op
   {::op.spec/op-key ::scenario.chan/move-rovers
    ::op.spec/op-type ::op.spec/fire-and-forget}
   channels
   {::scenario.spec/choose-location ::scenario.spec/closest
    ::scenario.spec/location-type ::scenario.spec/signal-tower
    ::scenario.spec/x-offset nil
    ::scenario.spec/y-offset nil})
  
  (keys @state*)
  
  ;;
  )

(defn create-proc-ops
  [channels state* opts]
  (let [{:keys [::scenario.main.chan/ops|]} channels

        scenario-exit|* (atom nil)

        start-proc-game
        (fn []
          (go
            (let [exit| @scenario-exit|*]
              (when (and exit| (not (closed? exit|)))
                (let [out| (chan 1)]
                  (put! exit| {::op.spec/out| out|})
                  (<! out|)
                  (close! exit|))))
            (let [exit| (chan 1)]
              (reset! scenario-exit|* exit|)
              (scenario.impl/create-proc-ops (merge
                                              channels
                                              {::scenario.chan/exit| exit|})
                                             state*
                                             {}))))]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::scenario.main.chan/init}
              (let [{:keys []} value]
                (println ::init)
                (scenario-api.chan/op
                 {::op.spec/op-key ::scenario-api.chan/generate
                  ::op.spec/op-type ::op.spec/fire-and-forget}
                 channels
                 {})
                (scenario.render/render-ui channels state* {}))

              {::op.spec/op-key ::scenario-api.chan/reset
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (<! (start-proc-game))
                #_(reset! state* {})
                #_(scenario-api.chan/op
                   {::op.spec/op-key ::scenario-api.chan/generate
                    ::op.spec/op-type ::op.spec/fire-and-forget}
                   channels
                   {}))

              {::op.spec/op-key ::scenario-api.chan/resume
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (println ::resume)
                (go
                  (loop [step 0]
                    (when (< step 10)
                      (let []
                        (when-let [[response port] (alts! [(player.chan/op
                                                            {::op.spec/op-key ::player.chan/next-move
                                                             ::op.spec/op-type ::op.spec/request-response
                                                             ::op.spec/op-orient ::op.spec/request}
                                                            channels
                                                            {::scenario.spec/step step})
                                                           (timeout 50)])]
                          (let [{:keys [::scenario.chan/op]} response]
                            (cond
                              op (scenario.chan/op
                                  op
                                  channels
                                  op)
                              :else (println (format "no operation on step %s" step)))
                            (<! (timeout 1000))
                            (recur (inc step)))))))))

              {::op.spec/op-key ::scenario-api.chan/pause
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (println ::pause))


              {::op.spec/op-key ::scenario-api.chan/replay
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (println ::replay))

              {::op.spec/op-key ::scenario-api.chan/generate
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []}
                    value

                    entities
                    (scenario.core/gen-entities
                     scenario.spec/x-size
                     scenario.spec/y-size)

                    {:keys [::scenario.spec/rovers
                            ::scenario.spec/locations]
                     :as entities-groups}
                    (scenario.core/entities-to-groups entities)

                    {:keys [::scenario.spec/entities-in-rovers-range
                            ::scenario.spec/entities-in-rovers-range-per-rover]
                     :as entities-in-range}
                    (scenario.core/entities-in-range
                     {::scenario.spec/entities entities
                      ::scenario.spec/rovers rovers})]
                (println ::generate)
                (swap! state* merge
                       {::scenario.spec/entities entities}
                       entities-groups
                       entities-in-range)
                (<! (start-proc-game)))
              ;
              )))
        (recur)))))

(def rsocket (rsocket.impl/create-proc-ops
              channels
              {::rsocket.spec/connection-side ::rsocket.spec/initiating
               ::rsocket.spec/host "localhost"
               ::rsocket.spec/port RSOCKET_PORT
               ::rsocket.spec/transport ::rsocket.spec/websocket}))

(def ops (create-proc-ops channels state* {}))

(defn ^:export main
  []
  (println ::main)
  (println ::RSOCKET_PORT RSOCKET_PORT)
  (scenario.main.chan/op
   {::op.spec/op-key ::scenario.main.chan/init}
   channels
   {}))


(do (main))


