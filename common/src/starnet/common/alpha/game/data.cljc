(ns starnet.common.alpha.game.data
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.core :refer [make-inst with-gen-fmap]]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]
   #?(:cljs [starnet.common.alpha.macros :refer-macros [defmethod-set derive-set]]
      :clj  [starnet.common.alpha.macros :refer [defmethod-set derive-set]])))

(s/def :g/uuid uuid?)
(s/def :g/events (s/coll-of :ev/event))
(def setof-game-roles #{:observer :player})
(s/def :g/role setof-game-roles)
(s/def :g/participants (s/map-of :u/uuid :g/role))
(s/def :g/host :u/uuid)
(def setof-game-status #{:created :opened :closed :started :finished})
(s/def :g/status setof-game-status)

(s/def :g.time/created inst?)
(s/def :g.time/opened inst?)
(s/def :g.time/closed inst?)
(s/def :g.time/started inst?)
(s/def :g.time/finished inst?)
(s/def :g.time/duration number?)

; events are the only true source
; else is derived
(s/def :g/state (s/keys :req [:g/events
                              :g/uuid
                              :g.time/created
                              :g.time/opened
                              :g.time/closed
                              :g.time/started
                              :g.time/finished
                              :g.time/duration
                              :g/participants
                              :g/status
                              :g/host]))

(comment

  (gen/generate (s/gen :g/state))

 ;;
  )

(s/def :ev/batch (with-gen-fmap
                   (s/keys :req [:ev/type :u/uuid :g/uuid :g/events]
                           :opt [])
                   #(assoc %  :ev/type :ev/batch)))

(s/def :ev.g/create (with-gen-fmap
                      (s/keys :req [:ev/type :u/uuid :g/uuid]
                              :opt [])
                      #(assoc %  :ev/type :ev.g/create)))

(s/def :ev.g/setup (with-gen-fmap
                     (s/keys :req [:ev/type :u/uuid :g/uuid]
                             :opt [])
                     #(assoc %  :ev/type :ev.g/setup)))

(s/def :ev.g/open (with-gen-fmap
                    (s/keys :req [:ev/type :u/uuid :g/uuid]
                            :opt [])
                    #(assoc %  :ev/type :ev.g/open)))

(s/def :ev.g/close (with-gen-fmap
                     (s/keys :req [:ev/type :u/uuid :g/uuid]
                             :opt [])
                     #(assoc %  :ev/type :ev.g/close)))

(s/def :ev.g/start (with-gen-fmap
                     (s/keys :req [:ev/type :u/uuid :g/uuid]
                             :opt [])
                     #(assoc %  :ev/type :ev.g/start)))

(s/def :ev.g/finish (with-gen-fmap
                      (s/and (s/keys :req [:ev/type :u/uuid]))
                      #(assoc %  :ev/type :ev.g/finish)))

(s/def :ev.g/join (with-gen-fmap
                    (s/keys :req [:ev/type :u/uuid :g/uuid]
                            :opt [])
                    #(assoc %  :ev/type :ev.g/join)))

(s/def :ev.g/leave (with-gen-fmap
                     (s/keys :req [:ev/type :u/uuid :g/uuid]
                             :opt [])
                     #(assoc %  :ev/type :ev.g/leave)))

(s/def :ev.g/select-role (with-gen-fmap
                           (s/keys :req [:ev/type :u/uuid :g/uuid :g/role]
                                   :opt [])
                           #(assoc %  :ev/type :ev.g/select-role)))

(s/def :ev.g/move-cape (with-gen-fmap
                         (s/keys :req [:ev/type :u/uuid :g/uuid
                                       :g.p/cape])
                         #(assoc %  :ev/type :ev.g/move-cape)))


(def eventset-event
  #{:ev.g/create
    :ev.g/select-role
    :ev.g/start :ev.g/join
    :ev.g/leave :ev.g/move-cape
    :ev.g/finish})

(s/def :ev/type eventset-event)

(defmulti ev (fn [x] (:ev/type x)))
(defmethod-set ev eventset-event)
(derive-set eventset-event :ev/event)
(s/def :ev/event (s/multi-spec ev :ev/type))

(comment

  (ancestors :ev.g/create)
  (ancestors :ev/event)
  (descendants :ev.g/create)
  (descendants :ev/event)



  ;;
  )

(defn spec-string-in-range
  [min max & {:keys [gen-char] :or {gen-char gen/char-alphanumeric}}]
  (s/with-gen
    string?
    #(gen/fmap (fn [v] (apply str v)) (gen/vector gen-char min max))))

(defn spec-number-in-range
  [min- max-]
  (s/with-gen
    number?
    #(gen/large-integer* {:min min- :max max-})))

(s/def :e/uuid uuid?)
(s/def :e/pos (s/tuple int? int?))
(s/def :e/type keyword?)
(def termsets
  {:health #{:enable :vitalize :energize :enliven :empower :invigorate :strengthen :heal :perform :efficiency}
   :spirit #{:inspire :encourage :vision :resolve :clarity :free :raise :faith :belief :sanity :determination}
   :mind #{:reason :understand :comprehend :wisdom :insight :decision-making :perspective
           :realization :intelligence :open-minded}
   :ability #{:capacity :competence :potential :efficiency :skill :aptitude :talent}
   :learn #{:knowledge :learn :seek :search :discover :practice :apply :experiment :listen}
   :design #{:abstraction :simplicity :contraint :elegance}
   :unhealth #{:deterioration :disease :decay :deficiency :unwellness :weakness}
   :fear #{:despair :doubt :dread :unease :blame :anxiety :concern :denial}
   :interface #{:primitive :advanced :simple :complex :limiting :extendable :intuitive}
   :field #{:drain :interfere :distract :limit :uplift :improve}})

(def gen-random-termset (gen/elements termsets))
(def gen-random-term (gen/bind gen-random-termset
                               #(gen/elements (second %))))

(s/def :e/qualities (s/with-gen (s/map-of keyword? number?)
                      #(gen/fmap
                        (fn [v] (into {} v))
                        (gen/vector (gen/tuple gen-random-term (gen/large-integer* {:min 0 :max 1000})) 3))))

(s/def :e.t/cape (s/keys :req [:e/type
                               :e/uuid
                               :e/pos
                               :e/qualities]))
(s/def :e.t/finding (s/keys :req [:e/type
                                  :e/uuid
                                  :e/pos
                                  :e/qualities]))
(s/def :e.t/fruit-tree (s/keys :req [:e/type
                                     :e/uuid
                                     :e/pos
                                     :e/qualities]))
(s/def :e.t/datacenter (s/keys :req [:e/type
                                     :e/uuid
                                     :e/pos
                                     :e/qualities]))
(s/def :e.t/nanitelab (s/keys :req [:e/type
                                    :e/uuid
                                    :e/pos
                                    :e/qualities]))
(s/def :e.t/droidshop (s/keys :req [:e/type
                                    :e/uuid
                                    :e/pos
                                    :e/qualities]))
(s/def :e.t/an-event (s/keys :req [:e/type
                                   :e/uuid
                                   :e/pos
                                   :e/qualities]))
(s/def :e.t/garden (s/keys :req [:e/type
                                 :e/uuid
                                 :e/pos
                                 :e/qualities]))
(s/def :e.t/teleport (s/keys :req [:e/type
                                   :e/uuid
                                   :e/pos
                                   :e/qualities]))
(s/def :e.t/repository (s/keys :req [:e/type
                                     :e/uuid
                                     :e/pos
                                     :e/qualities]))

(defn make-positions
  [x y]
  (->> (for [x (range 0 x)
             y (range 0 y)]
         [x y])))

(defn make-entities
  "A template: given opts, generates a set of entities for the map"
  [opts]
  (let [ps (make-positions 64 64)
        xs (gen/sample
            (gen/frequency [[50 (s/gen :e.t/finding)]
                            [10 (s/gen :e.t/fruit-tree)]
                            [10 (s/gen :e.t/datacenter)]
                            [10 (s/gen :e.t/nanitelab)]
                            [20 (s/gen :e.t/droidshop)]
                            [20 (s/gen :e.t/garden)]
                            [30 (s/gen :e.t/teleport)]
                            [30 (s/gen :e.t/repository)]])
            (count ps))]
    (map (fn [x p]
           (assoc x :e/pos p)) (shuffle xs) (shuffle ps))))

(comment

  (->> (make-entities {}) (vec) (take 5))

  (gen/generate (s/gen :e.t/garden))

  ;;
  )