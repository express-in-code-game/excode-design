(ns starnet.alpha.core.spec2
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.alpha.core.tmp :refer [with-gen-fmap ]]
   #?(:cljs [starnet.alpha.core.macros :refer-macros [defmethod-set]]
      :clj  [starnet.alpha.core.macros :refer [defmethod-set]])))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(defn spec-email
  []
  (let []
    (s/with-gen
      (s/and string? #(re-matches email-regex %))
      #(sgen/fmap (fn [s]
                    (str s "@gmail.com"))
                  (gen/such-that (fn [s] (not= s ""))
                                 gen/string-alphanumeric)))))

(defn spec-string-in-range
  [min max & {:keys [gen-char] :or {gen-char gen/char-alphanumeric}}]
  (s/with-gen
    string?
    #(gen/fmap (fn [v] (apply str v)) (gen/vector gen-char min max))))

(comment

  (gen/generate gen/string)
  (gen/generate gen/string-ascii)
  (gen/generate gen/string-alphanumeric)

  (gen/generate (s/gen (spec-string-in-range 4 16 :gen-char gen/char-ascii)))
  (gen/generate (s/gen (spec-string-in-range 4 16 :gen-char gen/char)))

  ;;
  )


(s/def :u/uuid uuid?) 
(s/def :u/username (spec-string-in-range 4 16 :gen-char gen/char-alphanumeric))
(s/def :u/fullname (spec-string-in-range 4 32 :gen-char gen/char-ascii))
(s/def :u/password (spec-string-in-range 8 64 :gen-char gen/char-alphanumeric))
(s/def :u/email (spec-email))

(s/def :u/user (s/keys :req [:u/uuid :u/username :u/email
                             :u/password :u/fullname ]))


(def eventset-event
  #{:ev.c/delete-record :ev.u/create
    :ev.u/update :ev.u/delete
    :ev.g.u/create :ev.g.u/update-role
    :ev.g.u/delete :ev.g.u/configure
    :ev.g.u/start :ev.g.u/join
    :ev.g.u/leave :ev.g.p/move-cape
    :ev.g.p/collect-tile-value
    :ev.g.a/finish-game})

(s/def :ev/type eventset-event)

(s/def :ev.c/delete-record (with-gen-fmap
                             (s/keys :req [:ev/type])
                             #(assoc %  :ev/type :ev.c/delete-record)))

(s/def :ev.u/create (with-gen-fmap
                      (s/keys :req [:ev/type :u/uuid :u/email :u/username]
                              :opt [])
                      #(assoc %  :ev/type :ev.u/create)))

(s/def :ev.u/update (with-gen-fmap
                      (s/keys :req [:ev/type]
                              :opt [:u/email :u/username])
                      #(assoc %  :ev/type :ev.u/update)))

(s/def :ev.u/delete (with-gen-fmap
                      (s/keys :req [:ev/type]
                              :opt [])
                      #(assoc %  :ev/type :ev.u/delete)))

(defmulti ev (fn [x] (:ev/type x)))
(defmethod-set ev eventset-event)
(s/def :ev/event (s/multi-spec ev :ev/type))

(def setof-ev-u-event
  #{:ev.u/create :ev.u/update :ev.u/delete})

(defmulti ev-user (fn [x] (:ev/type x)))
(defmethod-set ev-user setof-ev-u-event)
(s/def :ev.u/event (s/multi-spec ev-user :ev/type))


(s/fdef starnet.alpha.core.user/next-state-user
  :args (s/cat :state (s/nilable :u/user)
               :k uuid?
               :ev :ev.u/event)
  :ret (s/nilable :u/user))

(comment

  (ns-unmap 'starnet.alpha.core.user 'next-state-user)
  (stest/instrument ['starnet.alpha.core.user/next-state-user])
  (stest/unstrument ['starnet.alpha.core.user/next-state-user])

  ;;
  )


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

(s/def :g/game (s/keys :req [:g/uuid :g/status
                             :g/duration-ms :g/start-inst
                             :g/roles :g/player-states
                             :g/value-tiles :g/exit-teleports
                             :g/map-size]))

(s/def :ev.g.u/create (with-gen-fmap
                        (s/keys :req [:ev/type :u/uuid]
                                :opt [])
                        #(assoc %  :ev/type :ev.g.u/create)))

(s/def :ev.g.u/delete (with-gen-fmap
                        (s/keys :req [:ev/type]
                                :opt [])
                        #(assoc %  :ev/type :ev.g.u/delete)))

(s/def :ev.g.u/configure (with-gen-fmap
                           (s/keys :req [:ev/type :u/uuid :g/uuid]
                                   :opt [])
                           #(assoc %  :ev/type :ev.g.u/configure)))

(s/def :ev.g.u/start (with-gen-fmap
                       (s/keys :req [:ev/type :u/uuid :g/uuid]
                               :opt [])
                       #(assoc %  :ev/type :ev.g.u/start)))

(s/def :ev.g.u/join (with-gen-fmap
                      (s/keys :req [:ev/type :u/uuid :g/uuid]
                              :opt [])
                      #(assoc %  :ev/type :ev.g.u/join)))

(s/def :ev.g.u/leave (with-gen-fmap
                       (s/keys :req [:ev/type :u/uuid :g/uuid]
                               :opt [])
                       #(assoc %  :ev/type :ev.g.u/leave)))

(s/def :ev.g.u/update-role (with-gen-fmap
                             (s/keys :req [:ev/type :u/uuid :g/uuid :g.r/role]
                                     :opt [])
                             #(assoc %  :ev/type :ev.g.u/update-role)))

(s/def :ev.g.p/move-cape (with-gen-fmap
                           (s/keys :req [:ev/type :u/uuid :g/uuid
                                         :g.p/cape])
                           #(assoc %  :ev/type :ev.g.p/move-cape)))

(s/def :ev.g.p/collect-tile-value (with-gen-fmap
                                    (s/and (s/keys :req [:ev/type :u/uuid]))
                                    #(assoc %  :ev/type :ev.g.p/collect-tile-value)))

(s/def :ev.g.a/finish-game (with-gen-fmap
                             (s/and (s/keys :req [:ev/type :u/uuid]))
                             #(assoc %  :ev/type :ev.g.a/finish-game)))

(def setof-ev-g-p-event
  #{:ev.g.p/move-cape :ev.g.p/collect-tile-value})

(defmulti ev-game-player (fn [x] (:ev/type x)))
(defmethod-set ev-game-player setof-ev-g-p-event)
(s/def :ev.g.p/event (s/multi-spec ev-game-player :ev/type))

(defmulti ev-game-arbiter (fn [x] (:ev/type x)))
(defmethod ev-game-arbiter :ev.g.a/finish-game [x] :ev.g.a/finish-game)
(s/def :ev.g.a/event (s/multi-spec ev-game-arbiter :ev/type))

(def setof-ev-g-m-event
  #{:ev.g.p/move-cape :ev.g.p/collect-tile-value :ev.g.a/finish-game})

(defmulti ev-game-member (fn [x] (:ev/type x)))
(defmethod-set ev-game-member setof-ev-g-m-event)
(s/def :ev.g.m/event (s/multi-spec ev-game-member :ev/type))

(def setof-ev-g-u-event
  #{:ev.g.u/create :ev.g.u/delete :ev.c/delete-record
    :ev.g.u/configure :ev.g.u/start :ev.g.u/join
    :ev.g.u/leave})

(defmulti ev-game-user (fn [x] (:ev/type x)))
(defmethod-set ev-game-user setof-ev-g-u-event)
(s/def :ev.g.u/event (s/multi-spec ev-game-user :ev/type))

(def setof-ev-g-event
  #{:ev.g.u/create :ev.g.u/delete :ev.c/delete-record
    :ev.g.u/configure :ev.g.u/start :ev.g.u/join :ev.g.u/update-role
    :ev.g.u/leave :ev.g.p/move-cape
    :ev.g.p/collect-tile-value :ev.g.a/finish-game})

(defmulti ev-game (fn [x] (:ev/type x)))
(defmethod-set ev-game setof-ev-g-event)
(s/def :ev.g/event (s/multi-spec ev-game :ev/type))



(s/fdef starnet.alpha.core.game/make-game-state
  :args (s/cat)
  :ret :g/game)

(s/fdef starnet.alpha.core.game/next-state-game
  :args (s/cat :state (s/nilable :g/game)
               :k uuid?
               :ev :ev.g/event)
  :ret (s/nilable :g/game))

(comment

  (ns-unmap 'starnet.alpha.core.game 'next-state-game)
  (stest/instrument ['starnet.alpha.core.game/next-state-game])
  (stest/unstrument ['starnet.alpha.core.game/next-state-game])

  (gen/sample (s/gen :ev.g.u/update-role) 10)
  (gen/sample (s/gen :ev.g.u/create) 10)
  (gen/sample (s/gen :g.r/role) 10)


  (stest/check 'starnet.alpha.core.game/next-state-game)

  ;;
  )

(def topic-evtype-map
  {"alpha.user" #{:ev.u/create :ev.u/update :ev.u/delete}
   "alpha.game" #{:ev.g.u/create :ev.g.u/delete
                  :ev.g.u/join :ev.g.u/leave
                  :ev.g.u/configure :ev.g.u/start
                  :ev.g.p/move-cape :ev.g.p/collect-tile-value
                  :ev.g.a/finish-game}})

(def evtype-topic-map
  (->> topic-evtype-map
       (map (fn [[topic kset]]
              (map #(vector % topic) kset)))
       (mapcat identity)
       (into {})))

(def evtype-recordkey-map
  {:ev.u/create :u/uuid
   :ev.u/update :u/uuid
   :ev.u/delete :u/uuid
   :ev.g.u/configure :g/uuid})

(defn event-to-recordkey
  [ev]
  (or
   (-> ev :ev/type evtype-recordkey-map ev)
   (gen/generate gen/uuid)))

(defn event-to-topic
  [ev]
  (-> ev :ev/type evtype-topic-map))