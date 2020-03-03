(ns app.alpha.data.spec
  (:require [clojure.pprint :as pp]
            [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]))

(s/def :g.e/uuid uuid?)
(s/def :g.e/pos (s/tuple int? int?))
(s/def :g.e/numeric-value number?)
(s/def :g.e/type keyword?)

(s/def :g.e.type/teleport (s/keys :req [:g.e/type :g.e/uuid :g.e/pos]))
(s/def :g.e.type/cape (s/keys :req [:g.e/type :g.e/uuid  :g.e/pos]))
(s/def :g.e.type/value-tile (s/keys :req [:g.e/type :g.e/uuid :g.e/pos :g.e/numeric-value]))

(s/def :g.p/cape :g.e.type/cape)
(s/def :g.p/entities (s/keys :req [:g.p/cape]))
(s/def :g.p/sum number?)

(s/def :g.p/player (s/keys :req [:g.p/entities :g.p/sum]))

(s/def :g.r/host (s/nilable boolean?))
(s/def :g.r/player (s/nilable int?))
(s/def :g.r/observer (s/nilable boolean?))
(s/def :g.r/role (s/keys :req [:g.r/host :g.r/player :g.r/observer]))

(s/def :g/uuid uuid?)
(s/def :g/status #{:created :opened :started :finished})
(s/def :g/start-inst inst?)
(s/def :g/duration-ms number?)
(s/def :g/map-size (s/tuple int? int?))
(s/def :g/roles (s/map-of uuid? :g.r/role))
(s/def :g/player-states (s/map-of int? :g.p/player))
(s/def :g/exit-teleports (s/coll-of :g.e.type/teleport))
(s/def :g/value-tiles (s/coll-of :g.e.type/value-tile))


(s/def :g/game (s/keys :req [:g/uuid :g/status :g/start-inst
                             :g/duration-ms :g/map-size
                             :g/roles :g/player-states
                             :g/exit-teleports
                             :g/value-tiles]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def :u/uuid uuid?)
(s/def :record/uuid uuid?)
(s/def :u/username string?)
(s/def :u/email (s/with-gen
                  (s/and string? #(re-matches email-regex %))
                  #(gen/fmap (fn [s]
                               (str s "@gmail.com"))
                             (gen/such-that (fn [s] (not= s ""))
                                            (gen/string-alphanumeric)))))

(s/def :u/user (s/keys :req [:u/uuid :u/username :u/email]))

(s/def :ev/type keyword?)

(s/def :ev.c/delete-record (s/keys :req [:ev/type]
                                   :opt [:record/uuid]))

(s/def :ev.u/create (s/keys :req [:ev/type :u/uuid :u/email :u/username]
                            :opt []))
(s/def :ev.u/update (s/keys :req [:ev/type]
                                 :opt [:u/email :u/username]))
(s/def :ev.u/delete (s/keys :req [:ev/type]
                            :opt []))
(s/def :ev.g.u/create (s/keys :req [:ev/type :u/uuid]
                                 :opt []))

(s/def :ev.g.u/delete (s/keys :req [:ev/type]
                              :opt []))

(s/def :ev.g.u/delete (s/keys :req [:ev/type :u/uuid :g/uuid]
                                 :opt []))

(s/def :ev.g.u/configure (s/keys :req [:ev/type :u/uuid :g/uuid]
                                    :opt []))

(s/def :ev.g.u/start (s/keys :req [:ev/type :u/uuid :g/uuid]
                                :opt []))

(s/def :ev.g.u/join (s/keys :req [:ev/type :u/uuid :g/uuid]
                               :opt []))

(s/def :ev.g.u/leave (s/keys :req [:ev/type :u/uuid :g/uuid]
                                :opt []))

(s/def :ev.g.p/move-cape (s/keys :req [:ev/type :u/uuid :g/uuid
                                       :g.p/cape]))
(def gen-ev-p-move-cape (gen/fmap (fn [x]
                                    (merge
                                     x
                                     {:ev/type :ev.g.p/move-cape}))
                                  (s/gen :ev.g.p/move-cape)))


(s/def :ev.g.p/collect-tile-value (s/and (s/keys :req [:ev/type])))

(s/def :ev.g.a/finish-game (s/and (s/keys :req [:ev/type])))

(def gen-ev-a-finish-game (gen/fmap (fn [x]
                                      (merge
                                       x
                                       {:ev/type :ev.g.a/finish-game}))
                                    (s/gen :ev.g.a/finish-game)))

(defmulti ev (fn [x] (:ev/type x)))
(defmethod ev :ev.u/create [x] :ev.u/create)
(defmethod ev :ev.u/update [x] :ev.u/update)
(defmethod ev :ev.u/delete [x] :ev.u/delete)
(defmethod ev :ev.g.u/create [x] :ev.g.u/create)
(defmethod ev :ev.g.u/delete [x] :ev.g.u/delete)
(defmethod ev :ev.g.u/configure [x] :ev.g.u/configure)
(defmethod ev :ev.g.u/start [x] :ev.g.u/start)
(defmethod ev :ev.g.u/join [x] :ev.g.u/join)
(defmethod ev :ev.g.u/leave [x] :ev.g.u/leave)
(defmethod ev :ev.c/delete-record [x] :ev.c/delete-record)
(defmethod ev :ev.g.p/move-cape [x] :ev.g.p/move-cape)
(defmethod ev :ev.g.p/collect-tile-value [x] :ev.g.p/collect-tile-value)
(defmethod ev :ev.g.a/finish-game [x] :ev.g.a/finish-game)
(s/def :ev/event (s/multi-spec ev :ev/type))

(defmulti ev-game-player (fn [x] (:ev/type x)) )
(defmethod ev-game-player :ev.g.p/move-cape [x] :ev.g.p/move-cape)
(defmethod ev-game-player :ev.g.p/collect-tile-value [x] :ev.g.p/collect-tile-value)
(s/def :ev.g.p/event (s/multi-spec ev-game-player :ev/type))

(defmulti ev-game-arbiter (fn [x] (:ev/type x)))
(defmethod ev-game-arbiter :ev.g.a/finish-game [x] :ev.g.a/finish-game)
(s/def :ev.g.a/event (s/multi-spec ev-game-arbiter :ev/type))

(defmulti ev-game-member (fn [x] (:ev/type x)))
(defmethod ev-game-member :ev.g.p/move-cape [x] :ev.g.p/move-cape)
(defmethod ev-game-member :ev.g.p/collect-tile-value [x] :ev.g.p/collect-tile-value)
(defmethod ev-game-member :ev.g.a/finish-game [x] :ev.g.a/finish-game)
(s/def :ev.g.m/event (s/multi-spec ev-game-member :ev/type))

(defmulti ev-game-user (fn [x] (:ev/type x)))
(defmethod ev-game-user :ev.g.u/create [x] :ev.g.u/create)
(defmethod ev-game-user :ev.g.u/delete [x] :ev.g.u/delete)
(defmethod ev-game-user :ev.c/delete-record [x] :ev.c/delete-record)
(defmethod ev-game-user :ev.g.u/configure [x] :ev.g.u/configure)
(defmethod ev-game-user :ev.g.u/start [x] :ev.g.u/start)
(defmethod ev-game-user :ev.g.u/join [x] :ev.g.u/join)
(defmethod ev-game-user :ev.g.u/leave [x] :ev.g.u/leave)
(s/def :ev.g.u/event (s/multi-spec ev-game-user :ev/type))



(defmulti ev-game (fn [x] (:ev/type x)))
(defmethod ev-game :ev.g.u/create [x] :ev.g.u/create)
(defmethod ev-game :ev.g.u/delete [x] :ev.g.u/delete)
(defmethod ev-game :ev.c/delete-record [x] :ev.c/delete-record)
(defmethod ev-game :ev.g.u/configure [x] :ev.g.u/configure)
(defmethod ev-game :ev.g.u/start [x] :ev.g.u/start)
(defmethod ev-game :ev.g.u/join [x] :ev.g.u/join)
(defmethod ev-game :ev.g.u/leave [x] :ev.g.u/leave)
(defmethod ev-game :ev.g.p/move-cape [x] :ev.g.p/move-cape)
(defmethod ev-game :ev.g.p/collect-tile-value [x] :ev.g.p/collect-tile-value)
(defmethod ev-game :ev.g.a/finish-game [x] :ev.g.a/finish-game)
(s/def :ev.g/event (s/multi-spec ev-game :ev/type))


(defmulti ev-user (fn [x] (:ev/type x)))
(defmethod ev-user :ev.u/create [x] :ev.u/create)
(defmethod ev-user :ev.u/update [x] :ev.u/update)
(defmethod ev-user :ev.u/delete [x] :ev.u/delete)
(s/def :ev.u/event (s/multi-spec ev-user :ev/type))










