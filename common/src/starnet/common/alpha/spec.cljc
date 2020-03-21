(ns starnet.common.alpha.spec
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.core :refer [with-gen-fmap ]]
   #?(:cljs [starnet.common.alpha.macros :refer-macros [defmethods-for-a-set]]
      :clj  [starnet.common.alpha.macros :refer [defmethods-for-a-set]])))

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
(s/def :u/info string?)
(s/def :u/email (spec-email))

(s/def :u/user (s/keys :req [:u/uuid :u/username :u/email
                             :u/password :u/fullname :u/info]))


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





(s/def :g/uuid uuid?)
(def setof-game-status #{:created :opened :started :finished})
(s/def :g/status setof-game-status)
(s/def :g/start-inst inst?)
(s/def :g/duration-ms number?)
(s/def :g/map-size (s/tuple int? int?))
(s/def :g/player-states (s/map-of int? :g.p/player))
(s/def :g/exit-teleports (s/coll-of :g.e.type/teleport))
(s/def :g/value-tiles (s/coll-of :g.e.type/value-tile))


(s/def :g/events (s/coll-of :ev/event))
(def setof-game-roles #{:observer :player})
(s/def :g/role setof-game-roles)
(s/def :g.derived/roles (s/map-of :u/uuid :g/role))
(s/def :g.derived/host :u/uuid)

(s/def :g/game (s/keys :req [:g/events :g.derived/roles :g.derived/host]))

(comment 
 
  (gen/generate (s/gen :g/game))
  
 ;;
 )

(s/def :ev.g/create (with-gen-fmap
                      (s/keys :req [:ev/type :u/uuid]
                              :opt [])
                      #(assoc %  :ev/type :ev.g/create)))

(s/def :ev.g/delete (with-gen-fmap
                      (s/keys :req [:ev/type]
                              :opt [])
                      #(assoc %  :ev/type :ev.g/delete)))

(s/def :ev.g/configure (with-gen-fmap
                         (s/keys :req [:ev/type :u/uuid :g/uuid]
                                 :opt [])
                         #(assoc %  :ev/type :ev.g/configure)))

(s/def :ev.g/start (with-gen-fmap
                     (s/keys :req [:ev/type :u/uuid :g/uuid]
                             :opt [])
                     #(assoc %  :ev/type :ev.g/start)))

(s/def :ev.g/join (with-gen-fmap
                    (s/keys :req [:ev/type :u/uuid :g/uuid]
                            :opt [])
                    #(assoc %  :ev/type :ev.g/join)))

(s/def :ev.g/leave (with-gen-fmap
                     (s/keys :req [:ev/type :u/uuid :g/uuid]
                             :opt [])
                     #(assoc %  :ev/type :ev.g/leave)))

(s/def :ev.g/update-role (with-gen-fmap
                           (s/keys :req [:ev/type :u/uuid :g/uuid :g.r/role]
                                   :opt [])
                           #(assoc %  :ev/type :ev.g/update-role)))

(s/def :ev.g/move-cape (with-gen-fmap
                         (s/keys :req [:ev/type :u/uuid :g/uuid
                                       :g.p/cape])
                         #(assoc %  :ev/type :ev.g/move-cape)))

(s/def :ev.g/collect-tile-value (with-gen-fmap
                                  (s/and (s/keys :req [:ev/type :u/uuid]))
                                  #(assoc %  :ev/type :ev.g/collect-tile-value)))

(s/def :ev.g/finish-game (with-gen-fmap
                           (s/and (s/keys :req [:ev/type :u/uuid]))
                           #(assoc %  :ev/type :ev.g/finish-game)))

(def eventset-event
  #{:ev.g/create :ev.g/update-role
    :ev.g/delete :ev.g/configure
    :ev.g/start :ev.g/join
    :ev.g/leave :ev.g/move-cape
    :ev.g/collect-tile-value
    :ev.g/finish-game})

(s/def :ev/type eventset-event)

(defmulti ev (fn [x] (:ev/type x)))
(defmethods-for-a-set ev eventset-event)
(s/def :ev/event (s/multi-spec ev :ev/type))