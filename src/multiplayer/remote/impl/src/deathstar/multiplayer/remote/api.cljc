(ns deathstar.multiplayer.remote.api
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [deathstar.multiplayer.spec :as multiplayer.spec]
   [deathstar.multiplayer.remote.spec :as spec]
   ))



(defn create-channels
  []
  (let [ops| (chan 10)
        ops|m (mult ops|)
        release| (chan 1)]
    {::spec/ops| ops|
     ::spec/ops|m ops|m}))


(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::spec/ops|m]} channels
        {:keys [state]} ctx
        ops|t (tap ops|m (chan 10))
        release| (chan 1)]
    (go (loop []
          (when-let [[v port] (alts! [ops|t release|])]
            (condp = port
              release|
              (let [{:keys [out|]}]
                (doseq [c| [ops|t release|]]
                  (close! c|))
                (close! out|))

              ops|t
              (do
                (condp = (:op v)

                  ::spec/foo
                  (let []
                    (println ::spec/foo)))
                (recur))))))
    (reify
      p/Release
      (-release [_] (let [out| (chan 1)]
                      (put! release| {:out| out|})
                      out|)))))


(defn connect
  [_]
  (-connect _))

(defn disconnect
  [_]
  (-connect _))

(defn connected?
  [_]
  (-connected? _))

(defn release
  [_]
  (-release _))