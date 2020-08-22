(ns deathstar.multiplayer.hub.api
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [cljctools.cljc.core :as cljc.core]


   [deathstar.multiplayer.spec :as multiplayer.spec]
   [deathstar.multiplayer.hub.store :as hub.store]
   [deathstar.multiplayer.hub.store.spec :as hub.store.spec]
   [deathstar.multiplayer.hub.spec :as spec]
   [deathstar.multiplayer.hub.protocols :as p]))


(defn create-channels
  []
  (let [ops| (chan 10)
        ops|m (mult ops|)]
    {::ops| ops|
     ::ops|m ops|m}))

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::ops|]} channels
        {:keys [store]} ctx
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
                    ::spec/user-connected
                    (let [{:keys [out|]} v]
                      (println ::spec/user-connected)
                      (hub.store/assoc-user store data out|))

                    ::spec/user-disconnected
                    (let []
                      (println ::spec/user-disconnected)
                      (hub.store/dissoc-user store data out|))

                    ::spec/list-users
                    (let [{:keys [out|]} v]
                      (println ::spec/list-users)
                      (hub.store/list-users store data out|))

                    ::spec/list-games
                    (let [{:keys [out|]} v]
                      (println ::spec/list-games)
                      (hub.store/list-games store data out|))

                    ::spec/create-game
                    (let []
                      (println ::spec/create-game))

                    ::spec/delete-game
                    (let []
                      (println ::spec/delete-game))

                    ::spec/start-game
                    (let []
                      (println ::spec/start-game))

                    ::spec/join-game
                    (let []
                      (println ::spec/join-game))

                    ::spec/leave-game
                    (let []
                      (println ::spec/leave-game)))
                  (recur)))))))
    (reify
      p/Release
      (-release [_] (release)))))

