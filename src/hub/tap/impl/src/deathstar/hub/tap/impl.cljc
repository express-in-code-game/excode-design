(ns deathstar.hub.tap.remote.impl
  #?(:cljs (:require-macros [deathstar.hub.tap.remote.impl]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [cljctools.cljc.core :as cljc.core]
   [cljctools.csp.op.spec :as op.spec]
   [deathstar.user.spec :as user.spec]
   [deathstar.game.spec :as game.spec]
   [deathstar.hub.chan :as hub.chan]
   [deathstar.hub.spec :as hub.spec]
   [deathstar.hub.tap.remote.spec :as tap.remote.spec]
   ))


(defn create-default-state-data
  []
  {::user.spec/user nil
   ::user.spec/users {}
   ::game.spec/games {}})

(defn create-state
  []
  (atom (create-default-state-data)))

(defn toggle-loading
  ([state op-key]
   (toggle-loading state op-key (not (get-in @state [op-key ::tap.remote.spec/loading?]))))
  ([state op-key loading?]
   (swap! state update op-key assoc ::tap.remote.spec/loading?  loading?)))


(defn create-proc-ops
  [channels state]
  (let [{:keys [::hub.chan/ops|]} channels
        #_ops|t #_(tap ops|m (chan 10))]
    (go (loop []
          (when-let [[v port] (alts! [ops|])]
            (condp = port

              ops|
              (condp = (select-keys v [::op.spec/op-key ::op.spec/op-type])

                {::op.spec/op-key ::hub.chan/user-join
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [::user.spec/uuid ::op.spec/out|]} v]
                  (println ::user-join)
                  (toggle-loading state ::hub.chan/user-join)
                  (swap! state assoc ::user.spec/user v))

                {::op.spec/op-key ::hub.chan/user-leave
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [::user.spec/uuid]} v]
                  (println ::user-leave)
                  (toggle-loading state ::hub.chan/user-leave)
                  (swap! state dissoc ::user.spec/user))


                {::op.spec/op-key ::hub.chan/list-users
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys []} v]
                  (println ::list-users)
                  (toggle-loading state ::hub.chan/list-users)
                  (swap! state merge (select-keys v [::user.spec/users])))

                {::op.spec/op-key ::hub.chan/list-games
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [out|]} v]
                  (println ::list-games)
                  (toggle-loading state ::hub.chan/list-games))

                {::op.spec/op-key ::hub.chan/game-create
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [out|]} v]
                  (println ::game-create)
                  (toggle-loading state ::hub.chan/game-create)
                  (let [uuid (cljc.core/rand-uuid)
                        game (merge
                              (select-keys v [::user.spec/uuid ::game.spec/uuid])
                              {::game.spec/uuid uuid})]
                    (swap! state update ::game.spec/games assoc uuid game)))

                {::op.spec/op-key ::hub.chan/game-remove
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [out|]} v]
                  (println ::game-remove)
                  (toggle-loading state ::hub.chan/game-remove)
                  (swap! state update ::game.spec/games dissoc (::game.spec/uuid v)))

                {::op.spec/op-key ::hub.chan/game-start
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [out|]} v]
                  (println ::game-start)
                  (toggle-loading state ::hub.chan/game-start))

                {::op.spec/op-key ::hub.chan/game-join
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [out|]} v]
                  (println ::game-join)
                  (toggle-loading state ::hub.chan/game-join))

                {::op.spec/op-key ::hub.chan/game-leave
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [out|]} v]
                  (println ::hub.chan/game-leave)
                  (toggle-loading state ::hub.chan/game-leave))

                {::op.spec/op-key ::hub.chan/user-join
                 ::op.spec/op-type ::op.spec/response}
                (let [{:keys [::user.spec/uuid]} v]
                  (println ::user-join)
                  (toggle-loading state ::hub.chan/user-join))

                {::op.spec/op-key ::hub.chan/user-leave
                 ::op.spec/op-type ::op.spec/response}
                (let [{:keys [::user.spec/uuid]} v]
                  (println ::user-leave)
                  (toggle-loading state ::hub.chan/user-leave))

                {::op.spec/op-key ::hub.chan/list-users
                 ::op.spec/op-type ::op.spec/response}
                (let [{:keys [::user.spec/users]} v]
                  (println ::list-users)
                  (toggle-loading state ::hub.chan/list-users)
                  (swap! state assoc ::user.spec/users users))

                {::op.spec/op-key ::hub.chan/list-games
                 ::op.spec/op-type ::op.spec/response}
                (let [{:keys [::game.spec/games]} v]
                  (println ::list-users)
                  (toggle-loading state ::hub.chan/list-games)
                  (swap! state assoc ::game.spec/games games))

                {::op.spec/op-key ::hub.chan/game-create
                 ::op.spec/op-type ::op.spec/response}
                (let [{:keys [::game.spec/uuid]} v]
                  (println ::game-create)
                  (toggle-loading state ::hub.chan/game-create)
                  (swap! state update ::game.spec/games assoc uuid v))

                {::op.spec/op-key ::hub.chan/game-remove
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [::game.spec/uuid]} v]
                  (println ::game-remove)
                  (toggle-loading state ::hub.chan/game-remove)
                  (swap! state update ::game.spec/games dissoc uuid))

                {::op.spec/op-key ::hub.chan/game-start
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [::game.spec/uuid]} v]
                  (println ::hub.chan/game-start)
                  (toggle-loading state ::hub.chan/game-start))

                {::op.spec/op-key ::hub.chan/game-join
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys []} v]
                  (println ::hub.chan/game-join)
                  (toggle-loading state ::hub.chan/game-join))

                {::op.spec/op-key ::hub.chan/game-leave
                 ::op.spec/op-type ::op.spec/request}
                (let [{:keys [out|]} v]
                  (println ::hub.chan/game-leave)
                  (toggle-loading state ::hub.chan/game-leave))

                (do
                  (println ::no-matching-clause)
                  (println (type v))
                  (println v)))))
          (recur)))))

