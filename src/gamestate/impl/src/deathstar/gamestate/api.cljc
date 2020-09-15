(ns deathstar.gamestate.api
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [deathstar.gamestate.protocols :as p]
   [deathstar.gamestate.spec :as spec]))


(defn create-channels
  []
  (let [ops| (chan 10)
        ops|m (mult ops|)]
    {::spec/ops| ops|
     ::spec/ops|m ops|m}))


(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::spec/ops| ::spec/ops|m]} channels
        {:keys [state]} ctx
        ops|t (tap ops|m (chan 10))]
    (go (loop []
          (when-let [[v port] (alts! [ops|t])]
            (condp = port
              ops|t
              (condp = (:op v)
                ::spec/run-dev-simulation
                (let []
                  (println ::spec/run-dev-simulation))

                ::spec/run-release-simulation
                (let []
                  (println ::spec/run-release-simulation))

                ::spec/reset-dev-simulation
                (let []
                  (println ::spec/reset-dev-simulation)))))
          (recur)))
    #_(reify
        p/Simulation
        (-bar [_])
        p/Release
        (-release [_]))))