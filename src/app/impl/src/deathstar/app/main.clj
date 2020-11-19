(ns deathstar.app.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [cljctools.rsocket.spec :as rsocket.spec]
   [cljctools.rsocket.chan :as rsocket.chan]
   [cljctools.rsocket.impl :as rsocket.impl]
   [cljctools.rsocket.examples-java]
   [cljctools.rsocket.examples]

   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]

   [deathstar.peernode.spec :as peernode.spec]
   [deathstar.peernode.chan :as peernode.chan]))

(def channels (merge
               (app.chan/create-channels)
               (rsocket.chan/create-channels)
               (peernode.chan/create-channels)))

(pipe (::rsocket.chan/requests| channels) (::app.chan/ops| channels))
(pipe (::peernode.chan/ops| channels) (::rsocket.chan/ops| channels))


(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::peernode.chan/ops|]} channels]
    (go
      (loop []
        (when-let [[v port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys v [::op.spec/op-key ::op.spec/op-type])

              {::op.spec/op-key ::peernode.chan/init}
              (let [{:keys []} value]
                
                (println ::init)))))
        (recur)))))

(def rsocket (rsocket.impl/create-proc-ops
              channels
              {::rsocket.spec/connection-side ::rsocket.spec/initiating
               ::rsocket.spec/host "peernode"
               ::rsocket.spec/port 7000
               ::rsocket.spec/transport ::rsocket.spec/websocket}))

(def peernode (create-proc-ops channels {}))

(defn -main [& args]
  (println ::-main)
  (app.chan/op
   {::op.spec/op-key ::app.chan/init}
   channels
   {}))

(comment

  (cljc.core/rand-uuid)


  (go
    (println (<! (peernode.chan/op
                  {::op.spec/op-key ::peernode.chan/id
                   ::op.spec/op-type ::op.spec/request-response
                   ::op.spec/op-orient ::op.spec/request}
                  channels))))


  ;;
  )