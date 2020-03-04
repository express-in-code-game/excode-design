(ns common.alpha.spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   #?(:cljs [common.alpha.macros :refer-macros [defmethods-for-a-set]]
      :clj  [common.alpha.macros :refer [defmethods-for-a-set]])))

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

(def setof-game-status #{:created :opened :started :finished})

(s/def :g/uuid uuid?)
(s/def :g/status setof-game-status)
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
                  #(sgen/fmap (fn [s]
                               (str s "@gmail.com"))
                             (sgen/such-that (fn [s] (not= s ""))
                                            (sgen/string-alphanumeric)))))

(s/def :u/user (s/keys :req [:u/uuid :u/username :u/email]))

(def setof-ev-event
  #{:ev.c/delete-record :ev.u/create
    :ev.u/update :ev.u/delete
    :ev.g.u/create
    :ev.g.u/delete :ev.g.u/configure
    :ev.g.u/start :ev.g.u/join
    :ev.g.u/leave :ev.g.p/move-cape
    :ev.g.p/collect-tile-value
    :ev.g.a/finish-game})



(s/def :ev/type setof-ev-event)

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
(def gen-ev-p-move-cape (sgen/fmap (fn [x]
                                    (merge
                                     x
                                     {:ev/type :ev.g.p/move-cape}))
                                  (s/gen :ev.g.p/move-cape)))


(s/def :ev.g.p/collect-tile-value (s/and (s/keys :req [:ev/type])))

(s/def :ev.g.a/finish-game (s/and (s/keys :req [:ev/type])))

(def gen-ev-a-finish-game (sgen/fmap (fn [x]
                                      (merge
                                       x
                                       {:ev/type :ev.g.a/finish-game}))
                                    (s/gen :ev.g.a/finish-game)))



(defmulti ev (fn [x] (:ev/type x)))
(defmethods-for-a-set ev setof-ev-event)
(s/def :ev/event (s/multi-spec ev :ev/type))

(def setof-ev-g-p-event
  #{:ev.g.p/move-cape :ev.g.p/collect-tile-value})

(defmulti ev-game-player (fn [x] (:ev/type x)) )
(defmethods-for-a-set ev-game-player setof-ev-g-p-event)
(s/def :ev.g.p/event (s/multi-spec ev-game-player :ev/type))

(defmulti ev-game-arbiter (fn [x] (:ev/type x)))
(defmethod ev-game-arbiter :ev.g.a/finish-game [x] :ev.g.a/finish-game)
(s/def :ev.g.a/event (s/multi-spec ev-game-arbiter :ev/type))

(def setof-ev-g-m-event
  #{:ev.g.p/move-cape :ev.g.p/collect-tile-value :ev.g.a/finish-game})

(defmulti ev-game-member (fn [x] (:ev/type x)))
(defmethods-for-a-set ev-game-member setof-ev-g-m-event)
(s/def :ev.g.m/event (s/multi-spec ev-game-member :ev/type))

(def setof-ev-g-u-event
  #{:ev.g.u/create :ev.g.u/delete :ev.c/delete-record
    :ev.g.u/configure :ev.g.u/start :ev.g.u/join
    :ev.g.u/leave})

(defmulti ev-game-user (fn [x] (:ev/type x)))
(defmethods-for-a-set ev-game-user setof-ev-g-u-event)
(s/def :ev.g.u/event (s/multi-spec ev-game-user :ev/type))

(def setof-ev-g-event
  #{:ev.g.u/create :ev.g.u/delete :ev.c/delete-record
    :ev.g.u/configure :ev.g.u/start :ev.g.u/join
    :ev.g.u/leave :ev.g.p/move-cape
    :ev.g.p/collect-tile-value :ev.g.a/finish-game})

(defmulti ev-game (fn [x] (:ev/type x)))
(defmethods-for-a-set ev-game setof-ev-g-event)
(s/def :ev.g/event (s/multi-spec ev-game :ev/type))

(def setof-ev-u-event
  #{:ev.u/create :ev.u/update :ev.u/delete})

(defmulti ev-user (fn [x] (:ev/type x)))
(defmethods-for-a-set ev-user setof-ev-u-event)
(s/def :ev.u/event (s/multi-spec ev-user :ev/type))










