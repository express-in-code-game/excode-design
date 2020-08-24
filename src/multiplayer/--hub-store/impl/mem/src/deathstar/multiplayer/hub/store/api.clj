(ns deathstar.multiplayer.hub.store.api
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [cljctools.cljc.core :as cljc.core]

   [deathstar.multiplayer.spec :as multiplayer.spec]
   [deathstar.multiplayer.protocols :as multiplayer.p]
   [deathstar.multiplayer.hub.store.spec :as spec]))




(defn create-default-state-data
  []
  {::multiplayer.spec/users {}
   ::multiplayer.spec/games {}})

(defn create-state
  []
  (atom (create-default-state-data)))

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::ops|]} channels
        {:keys [state]} ctx
        ops| (tap ops| (chan 10))
        close|| (repeatedly 16 #(chan 1))
        release (fn []
                  (doseq [close| close||]
                    (close! close|)))]
    (doseq [close| close||]
      (go (loop []
            (when-let [[v port] (alts! [ops| close|])]
              (condp = port
                close|
                (do nil)
                
                ops|
                (do
                  (condp = (:op v)
                    ::spec/some-op
                    (do nil))
                  (recur)))))))
    (reify
      multiplayer.p/Release
      (-release [_] (release)))))