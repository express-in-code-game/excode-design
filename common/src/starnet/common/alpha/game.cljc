(ns starnet.common.alpha.game
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
   [clojure.walk :as walk]
   [datascript.core :as d]
   #?(:cljs [reagent.core :as r])
   #?(:cljs [starnet.common.alpha.macros :refer-macros [defmethods-for-a-set]]
      :clj  [starnet.common.alpha.macros :refer [defmethods-for-a-set]])))



(s/def :g.e/uuid uuid?)
(s/def :g.e/pos (s/tuple int? int?))
(s/def :g.e/numeric-value number?)
(s/def :g.e/type keyword?)

(s/def :g.e.type/teleport (s/keys :req [:g.e/type
                                        :g.e/uuid
                                        :g.e/pos]))
(s/def :g.e.type/cape (s/keys :req [:g.e/type
                                    :g.e/uuid
                                    :g.e/pos]))


(s/def :g.p/cape :g.e.type/cape)
(s/def :g.p/entities (s/keys :req [:g.p/cape]))
(s/def :g.p/sum number?)

(s/def :g.p/player (s/keys :req [:g.p/entities
                                 :g.p/sum]))

(s/def :g.r/host (s/nilable boolean?))
(s/def :g.r/player (s/nilable int?))
(s/def :g.r/observer (s/nilable boolean?))

(s/def :g/uuid uuid?)


(s/def :g/map-size (s/tuple int? int?))
(s/def :g/player-states (s/map-of int? :g.p/player))
(s/def :g/exit-teleports (s/coll-of :g.e.type/teleport))

(s/def :g.e.type/tile (s/keys :req [:g.e/type
                                    :g.e/uuid
                                    :g.e/pos
                                    :g.e/numeric-value]))



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

(s/def :ev.g/collect-tile-value (with-gen-fmap
                                  (s/and (s/keys :req [:ev/type :u/uuid]))
                                  #(assoc %  :ev/type :ev.g/collect-tile-value)))



(def eventset-event
  #{:ev.g/create
    :ev.g/select-role
    :ev.g/start :ev.g/join
    :ev.g/leave :ev.g/move-cape
    :ev.g/collect-tile-value
    :ev.g/finish})

(s/def :ev/type eventset-event)

(defmulti ev (fn [x] (:ev/type x)))
(defmethods-for-a-set ev eventset-event)
(s/def :ev/event (s/multi-spec ev :ev/type))

(defn make-state-core
  ([]
   (make-state-core {}))
  ([opts]
   (merge {:g/uuid (gen/generate gen/uuid)
           :g/events []}
          (select-keys opts [:g/events :g/uuid]))))

(declare next-state*)

(defn next-state
  ([state k ev]
   (next-state state k ev nil))
  ([state k ev tags]
   (cond
     (and (coll? tags) (empty? tags)) state
     (list? tags) (next-state (next-state state k ev (first tags)) k ev (rest tags))
     (set? tags) (next-state* state k ev tags)
     (vector? tags) (next-state* state k ev tags)
     :else (next-state* state k ev tags))))

(defmulti next-state*
  "Returns the next :g.state/derived-core"
  {:arglists '([state key event tags])}
  (fn [state k ev tags]
    (if tags [(:ev/type ev) tags] [(:ev/type ev)])))

(defmethod next-state* [:ev.g/create :plain]
  [state k ev _]
  (-> state
      (update  :plain (fnil inc 0))
      (assoc :k :plain)))

(defmethod next-state* [:ev.g/create :derived]
  [state k ev _]
  (-> state
      (update  :derived (fnil inc 0))
      (assoc :k :derived)))

(next-state {} nil {:ev/type :ev.g/create} '(:plain :derived))


(isa? #{:b :a} #{:a :b})
(isa? #{:b :c :a #{:b :a}} #{:a :b :c #{:a :b}})

(defmulti next-state-derived-core
  "Returns the next :g.state/derived-core"
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-state-derived-core [:ev.g/create]
  [state k ev]
  (let [{:keys [u/uuid]} ev]
    (-> state
        (assoc :g/status :created)
        (assoc :g/host uuid)
        (merge (select-keys ev [:g.time/created])))))

(defmethod next-state-derived-core [:ev.g/setup]
  [state k ev]
  (-> state
      (merge (select-keys ev [:g.time/duration]))))

(defmethod next-state-derived-core [:ev.g/open]
  [state k ev]
  (-> state
      (assoc :g/status :opened)
      (merge (select-keys ev [:g.time/opened]))))

(defmethod next-state-derived-core [:ev.g/close]
  [state k ev]
  (-> state
      (assoc :g/status :closed)
      (merge (select-keys ev [:g.time/closed]))))

(defmethod next-state-derived-core [:ev.g/start]
  [state k ev]
  (-> state
      (assoc :g/status :started)
      (merge (select-keys ev [:g.time/started]))))

(defmethod next-state-derived-core [:ev.g/finish]
  [state k ev]
  (-> state
      (assoc :g/status :finished)
      (merge (select-keys ev [:g.time/finished]))))

(defmethod next-state-derived-core [:ev.g/join]
  [state k ev]
  (let [{:keys [u/uuid]} ev]
    (-> state
        (update-in [:g/participants] assoc uuid :observer))))

(defmethod next-state-derived-core [:ev.g/leave]
  [state k ev]
  (let [{:keys [u/uuid]} ev]
    (-> state
        (update-in [:g/participants] dissoc uuid))))

(defmethod next-state-derived-core [:ev.g/select-role]
  [state k ev]
  (let [{:keys [u/uuid g/role]} ev]
    (-> state
        (update-in [:g/participants] assoc uuid role))))


(defmethod next-state-derived-core :default
  [state k ev]
  state)


(defmulti next-state-core
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-state-core [:ev/batch]
  [state k ev]
  (let [{:keys [g/events]} ev]
    (as-> state o
      (update o :g/events #(-> % (concat events) (vec)))
      (reduce (fn [agg v]
                (update agg :g.state/derived-core next-state-derived-core k v)) o events))))

(defmethod next-state-core :default
  [state k ev]
  (-> state
      (update :g/events #(-> % (conj ev)))
      (update :g.state/derived-core next-state-derived-core k ev)))

(defmulti next-state-derived-event
  {:arglists '([store key event])}
  (fn [store k ev] [(:ev/type ev)]))

(defmethod next-state-derived-event [:ev.g/start]
  [store k ev]
  (let []
    
    
    ))

(defmethod next-state-derived-event :default
  [store k ev]
  (let []))

(defn next-state-derived
  [store k ev]
  (let [state* (store :g/state)
        state-core (next-state-core @state* k ev)]
    (swap! state* merge state-core)
    (next-state-derived-event store k ev))
  nil)

(defn make-game-channels
  []
  (let [ch-game (chan (sliding-buffer 10))
        ch-game-events (chan (sliding-buffer 10))
        ch-inputs (chan (sliding-buffer 10))]
    {:ch-game ch-game
     :ch-game-events ch-game-events
     :ch-inputs ch-inputs}))

#?(:cljs (defn make-default-store
           [default-state-core]
           (let [state-core (r/atom default-state-core)
                 derived-core (r/cursor state-core [:g.state/derived-core])]
             {:g/state state-core
              :g.state/derived-core derived-core
              :db/ds nil})))

;for repl only
(defonce ^:private -store nil)
(defonce ^:private -channels nil)
#?(:cljs (defn proc-game
           [{:keys [ch-game ch-game-events ch-inputs] :as channels} store]
           (let []
             (set! -store store)
             (set! -channels channels)
             (go (loop []
                   (if-let [[v port] (alts! [ch-game-events ch-inputs])]
                     (condp = port
                       ch-game-events (let []
                                        (next-state-derived store nil v)
                                        (recur))
                       ch-inputs (let []
                                   (println v)
                                   (recur)))))
                 (println "proc-game closing")))))

(comment

  (def guuid (-> @(-store :g/state) :g/uuid))
  (def u1 (gen/generate (s/gen :u/user)))

  (put! (-channels :ch-game-events) {:ev/type :ev.g/create
                                     :g/uuid guuid
                                     :u/uuid (:u/uuid u1)})
  (put! (-channels :ch-game-events) {:ev/type :ev.g/close
                                     :g/uuid guuid
                                     :u/uuid (:u/uuid u1)})
  
  (put! (-channels :ch-game-events) {:ev/type :ev.g/start
                                     :g/uuid guuid
                                     :u/uuid (:u/uuid u1)})

  
  
  ;;
  )

#?(:cljs
   (defn rc-game
     [channels ratoms]
     (let [{:keys [ch-inputs]} channels
           uuid* (r/cursor (ratoms :g/state) [:g/uuid])
           status* (r/cursor (ratoms :g.state/derived-core) [:g/status])]
       (fn [_ _]
         (let [uuid @uuid*
               status @status*]
           [:<>
            [:div "rc-game"]
            [:div  uuid]
            [:div  status]]
           )))))

(comment 
  
  ;;
  )

(defn gen-tiles
  []
  
  
  )