(ns deathstar.player-base.impl
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

   [deathstar.player-base.spec :as player-base.spec]
   [deathstar.player-base.chan :as player-base.chan]))


(goog-define RSOCKET_PORT 0)

(set! RSOCKET_PORT (str (subs js/location.port 0 2) (subs (str RSOCKET_PORT) 2)))

(comment


  ;;
  )

(defn create-proc-ops
  [channels opts]
  (let [{:keys [::player-base.chan/ops|
                ::player-base.chan/release|]} channels]
    (go
      (loop []
        (when-let [[value port] (alts! [ops| release|])]
          (condp = port

            release|
            (let [{:keys [::op.spec/out|]} value]
              (close! out|))
            
            ops|
            (do
              (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

                {::op.spec/op-key ::app.chan/init
                 ::op.spec/op-type ::op.spec/fire-and-forget}
                (println ::init))
              (recur))))))))