(ns deathstar.hub.impl
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [cljctools.cljc.core :as cljc.core]

   [cljctools.csp.op.spec :as op.spec]
   [deathstar.user.spec :as user.spec]
   [deathstar.game.spec :as game.spec]
   [deathstar.hub.chan :as hub.chan]))

(defn create-default-state-data
  []
  {::user.spec/users {}
   ::game.spec/games {}})

(defn create-state
  []
  (atom (create-default-state-data)))

(defn create-proc-ops
  [channels state opts]
  (let [{:keys [::hub.chan/ops|m]} channels
        ops|t (tap ops|m (chan 10))
        close|| (repeatedly 16 #(chan 1))
        release (fn []
                  (doseq [close| close||]
                    (close! close|)))]
    (doseq [close| close||]
      (go (loop []
            (when-let [[v port] (alts! [ops|t close|])]
              (condp = port
                close|
                (do nil)
                
                ops|t
                (do
                  (condp = (select-keys v [::op.spec/op-key ::op.spec/op-type])

                    {::op.spec/op-key ::hub.chan/user-join
                     ::op.spec/op-type ::op.spec/request}
                    (let [{:keys [::user.spec/uuid ::op.spec/out|]} v
                          user-data (select-keys v [::user.spec/uuid])]
                      (println ::user-join)
                      (swap! state update ::user.spec/users
                             assoc uuid user-data)
                      (hub.chan/op
                       {::op.spec/op-key ::hub.chan/user-join
                        ::op.spec/op-type ::op.spec/response}
                       channels out| user-data))

                    {::op.spec/op-key ::hub.chan/user-leave
                     ::op.spec/op-type ::op.spec/request}
                    (let [{:keys [::user.spec/uuid ::op.spec/out|]} v]
                      (println ::user-leave)
                      (swap! state update ::user.spec/users dissoc uuid)
                      (hub.chan/op
                       {::op.spec/op-key ::hub.chan/user-leave
                        ::op.spec/op-type ::op.spec/response}
                       channels out| (select-keys v [::user.spec/uuid])))


                    {::op.spec/op-key ::hub.chan/list-users
                     ::op.spec/op-type ::op.spec/request}
                    (let [{:keys [::op.spec/out|]} v]
                      (println ::list-users)
                      (hub.chan/op
                       {::op.spec/op-key ::hub.chan/list-users
                        ::op.spec/op-type ::op.spec/response}
                       channels out| (get @state ::user.spec/users)))

                    {::op.spec/op-key ::hub.chan/list-games
                     ::op.spec/op-type ::op.spec/request}
                    (let [{:keys [out|]} v]
                      (println ::list-games)
                      (hub.chan/op
                       {::op.spec/op-key ::hub.chan/list-users
                        ::op.spec/op-type ::op.spec/response}
                       out| (get @state ::game.spec/games)))

                    {::op.spec/op-key ::hub.chan/game-create
                     ::op.spec/op-type ::op.spec/request}
                    (let [{:keys [out|]} v]
                      (println ::game-create)
                      (let [uuid (cljc.core/rand-uuid)
                            game (merge
                                  (select-keys v [::user.spec/uuid ::game.spec/uuid])
                                  {::game.spec/uuid uuid})]
                        (swap! state update ::game.spec/games assoc uuid game)
                        (hub.chan/op
                         {::op.spec/op-key ::hub.chan/game-create
                          ::op.spec/op-type ::op.spec/response}
                         out| uuid)))

                    {::op.spec/op-key ::hub.chan/game-remove
                     ::op.spec/op-type ::op.spec/request}
                    (let [{:keys [out|]} v]
                      (println ::game-remove)
                      (swap! state update ::game.spec/games dissoc (::game.spec/uuid v))
                      (hub.chan/op
                       {::op.spec/op-key ::hub.chan/game-remove
                        ::op.spec/op-type ::op.spec/response}
                       out| (::game.spec/uuid v)))

                    {::op.spec/op-key ::hub.chan/game-start
                     ::op.spec/op-type ::op.spec/request}
                    (let [{:keys [out|]} v]
                      (println ::hub.chan/game-start)
                      (hub.chan/op
                       {::op.spec/op-key ::hub.chan/game-start
                        ::op.spec/op-type ::op.spec/response}
                       out| (::game.spec/uuid v)))

                    {::op.spec/op-key ::hub.chan/game-join
                     ::op.spec/op-type ::op.spec/request}
                    (let [{:keys [out|]} v]
                      (println ::hub.chan/game-join)
                      (hub.chan/op
                       {::op.spec/op-key ::hub.chan/game-join
                        ::op.spec/op-type ::op.spec/response}
                       out| (::game.spec/uuid v)))

                    {::op.spec/op-key ::hub.chan/game-leave
                     ::op.spec/op-type ::op.spec/request}
                    (let [{:keys [out|]} v]
                      (println ::hub.chan/game-leave)
                      (hub.chan/op
                       {::op.spec/op-key ::hub.chan/game-leave
                        ::op.spec/op-type ::op.spec/response}
                       out| (::game.spec/uuid v)))
                    (do
                      (println ::no-matching-clause)
                      (println (type v))
                      (println v)))
                  (recur)))))))
    #_(reify
        p/Release
        (-release [_] (release)))))

