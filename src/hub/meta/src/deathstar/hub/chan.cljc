(ns deathstar.hub.chan
  #?(:cljs (:require-macros [deathstar.hub.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [cljctools.csp.op.spec :as op.spec]
   [deathstar.user.spec :as user.spec]
   [deathstar.game.spec :as game.spec]))

(do (clojure.spec.alpha/check-asserts true))

(defmulti ^{:private true} op* op.spec/op-spec-dispatch-fn)
(s/def ::op (s/multi-spec op* op.spec/op-spec-retag-fn))
(defmulti op op.spec/op-dispatch-fn)

(s/def ::user| some?)

(defn create-channels
  []
  (let [ops| (chan 10)]
    {::ops| ops|}))


(defmethod op*
  {::op.spec/op-key ::user-connected} [_]
  (s/keys :req [::hub.chan/user|]))

(defmethod op
  {::op.spec/op-key ::user-connected}
  [op-meta channels user|]
  (put! (::ops| channels) (merge op-meta
                                 {::hub.chan/user| user|})))


(defmethod op*
  {::op.spec/op-key ::user-join
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req [::user.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::user-join
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels opts]
   (op op-meta channels opts  (chan 1)))
  ([op-meta channels opts out|]
   (put! (::ops| channels) (merge op-meta
                                  opts
                                  {::op.spec/out| out|}))
   out|))

(defmethod op*
  {::op.spec/op-key ::user-join
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req [::user.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::user-join
   ::op.spec/op-type ::op.spec/response}
  [op-meta channels out| opts]
  (let [value (merge op-meta
                     opts)]
    (put! out| value)))

(defmethod op*
  {::op.spec/op-key ::user-leave
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req [::user.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::user-leave
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels opts]
   (op op-meta channels opts  (chan 1)))
  ([op-meta channels opts out|]
   (put! (::ops| channels) (merge op-meta
                                  opts
                                  {::op.spec/out| out|}))
   out|))

(defmethod op*
  {::op.spec/op-key ::user-leave
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req [::user.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::user-leave
   ::op.spec/op-type ::op.spec/response}
  [op-meta channels out| uuid]
  (let [value (merge op-meta
                     {::user.spec/uuid uuid})]
    (put! out| value)))

(defmethod op*
  {::op.spec/op-key ::list-users
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req []))

(defmethod op
  {::op.spec/op-key ::list-users
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels]
   (op op-meta channels (chan 1)))
  ([op-meta channels out|]
   (put! (::ops| channels) (merge op-meta
                                  {::op.spec/out| out|}))
   out|))

(defmethod op*
  {::op.spec/op-key ::list-users
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req [::user.spec/users]))

(defmethod op
  {::op.spec/op-key ::list-users
   ::op.spec/op-type ::op.spec/response}
  [op-meta channels out| users]
  (let [value (merge op-meta
                     {::user.spec/users users})]
    (put! out| value)))


(defmethod op*
  {::op.spec/op-key ::list-games
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req []))

(defmethod op
  {::op.spec/op-key ::list-games
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels]
   (op op-meta channels  (chan 1)))
  ([op-meta channels  out|]
   (put! (::ops| channels) (merge op-meta
                                  {::op.spec/out| out|}))
   out|))

(defmethod op*
  {::op.spec/op-key ::list-games
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req [::game.spec/games]))

(defmethod op
  {::op.spec/op-key ::list-games
   ::op.spec/op-type ::op.spec/response}
  [op-meta channels out| games]
  (let [value (merge op-meta
                     {::game.spec/games games})]
    (put! out| value)))


(defmethod op*
  {::op.spec/op-key ::game-create
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req [::user.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::game-create
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels opts]
   (op op-meta channels opts  (chan 1)))
  ([op-meta channels opts out|]
   (put! (::ops| channels) (merge op-meta
                                  opts
                                  {::op.spec/out| out|}))
   out|))

(defmethod op*
  {::op.spec/op-key ::game-create
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req [::game.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::game-create
   ::op.spec/op-type ::op.spec/response}
  [op-meta channels out| uuid]
  (let [value (merge op-meta
                     {::game.spec/uuid uuid})]
    (put! out| value)))


(defmethod op*
  {::op.spec/op-key ::game-remove
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req [::user.spec/uuid ::game.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::game-remove
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels opts]
   (op op-meta channels opts  (chan 1)))
  ([op-meta channels opts out|]
   (put! (::ops| channels) (merge op-meta
                                  opts
                                  {::op.spec/out| out|}))
   out|))

(defmethod op*
  {::op.spec/op-key ::game-remove
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req [::game.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::game-remove
   ::op.spec/op-type ::op.spec/response}
  [op-meta channels out| uuid]
  (let [value (merge op-meta
                     {::game.spec/uuid uuid})]
    (put! out| value)))


(defmethod op*
  {::op.spec/op-key ::game-join
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req [::user.spec/uuid ::game.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::game-join
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels opts]
   (op op-meta channels opts  (chan 1)))
  ([op-meta channels opts out|]
   (put! (::ops| channels) (merge op-meta
                                  opts
                                  {::op.spec/out| out|}))
   out|))

(defmethod op*
  {::op.spec/op-key ::game-join
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req [::game.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::game-join
   ::op.spec/op-type ::op.spec/response}
  [op-meta channels out| uuid]
  (let [value (merge op-meta
                     {::game.spec/uuid uuid})]
    (put! out| value)))


(defmethod op*
  {::op.spec/op-key ::game-leave
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req [::user.spec/uuid ::game.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::game-leave
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels opts]
   (op op-meta channels opts  (chan 1)))
  ([op-meta channels opts out|]
   (put! (::ops| channels) (merge op-meta
                                  opts
                                  {::op.spec/out| out|}))
   out|))

(defmethod op*
  {::op.spec/op-key ::game-leave
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req [::game.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::game-leave
   ::op.spec/op-type ::op.spec/response}
  [op-meta channels out| uuid]
  (let [value (merge op-meta
                     {::game.spec/uuid uuid})]
    (put! out| value)))


(defmethod op*
  {::op.spec/op-key ::game-start
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req [::user.spec/uuid ::game.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::game-start
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels opts]
   (op op-meta channels opts  (chan 1)))
  ([op-meta channels opts out|]
   (put! (::ops| channels) (merge op-meta
                                  opts
                                  {::op.spec/out| out|}))
   out|))

(defmethod op*
  {::op.spec/op-key ::game-start
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req [::game.spec/uuid]))

(defmethod op
  {::op.spec/op-key ::game-start
   ::op.spec/op-type ::op.spec/response}
  [op-meta channels out| uuid]
  (let [value (merge op-meta
                     {::game.spec/uuid uuid})]
    (put! out| value)))
