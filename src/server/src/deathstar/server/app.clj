(ns deathstar.server.app
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [deathstar.server.spec :as server.sp]))

(defn create-channels
  []
  (let [server-ops| (chan 10)
        server-ops||m (mult server-ops||)]
    {:server-ops| server-ops|
     :server-ops|m server-ops|m}))

(defprotocol Server
  (-start [_])
  (-stop [_]))

(defn create-proc-ops
  [{:keys [shadow-ops| shadow-ops|m] :as channels} ctx]
  (let [shadow-ops|t (tap shadow-ops|m (chan 10))]
    (go
      (loop []
        (when-let [[v port] (alts! [shadow-ops|t])]
          (condp = port
            shadow-ops|t (condp = (:op v)

                           (worker.sp/op
                            :some|
                            :hello)
                           (let []
                             (println "shadow-ops")))))
        (recur))
      (println "; proc-ops go-block exiting"))
    (reify
      Server
      (-start [_])
      (-stop [_]))))
