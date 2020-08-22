(ns deathstar.multiplayer.user.api
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [deathstar.multiplayer.spec :as spec]))



(defn create-channels
  []
  (let [ops| (chan 10)
        ops|m (mult ops|)]
    {::ops| ops|
     ::ops|m ops|m}))


(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::ops|m]} channels
        {:keys [state]} ctx
        ops|t (tap ops|m (chan 10))]
    (go (loop []
          (when-let [[v port] (alts! [ops|t])]
            (condp = port
              ops|t
              (condp = (:op v)
                ::spec/foo
                (let []
                  (println ::spec/foo)))))
          (recur)))
    #_(reify
        p/Simulation
        (-bar [_])
        p/Release
        (-release [_]))))