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

(defn make-state
  ([]
   (make-state {}))
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
     (and (vector? tags)
          (descendants (first tags))) (cond
                                        (-> (first tags)
                                            (descendants)
                                            (:ev/type ev)) (next-state* state k ev tags)
                                        :else state)
     (and (coll? tags) (empty? tags)) state
     (set? tags) (next-state* state k ev [(:ev/type ev) tags])
     (vector? tags) (next-state* state k ev [(:ev/type ev) tags])
     (list? tags) (next-state (next-state state k ev (first tags)) k ev (rest tags))
     :else (next-state* state k ev tags))))

(comment

  (ancestors :ev.g/create)
  (ancestors :ev/event)
  (descendants :ev.g/create)
  (descendants :ev/event)

  (ns-unmap *ns* 'next-state*)
  
  (next-state nil nil {:ev/type :ev.g/create} '([:ev/event #{:plain}] #{:plain} ))
  
  ;;
  )

(defmulti next-state*
  {:arglists '([state key event dispatch-v])}
  (fn [state k ev dispatch-v] dispatch-v))

(defmethod next-state* :default
  [state k ev dispatch-v]
  #_(println (format "; warning: next-state* :default invoked %s %s" (:ev/type ev) dispatch-v))
  state)

(defmethod next-state* [:ev/event #{:plain}]
  [state k ev _]
  (-> state
      (update :g/events #(-> % (conj ev)))))

(defmethod next-state* [:ev/event #{:derived}]
  [state k ev _]
  (let []
    (swap! (:ra.g/state state) merge (dissoc state :ra.g/state :g/events :db/ds))
    state))

(defmethod next-state* [:ev/batch #{:plain}]
  [state k ev _]
  (let [{:keys [g/events]} ev]
    (as-> state o
      (update o :g/events #(-> % (concat events) (vec)))
      (reduce (fn [state- ev-]
                (next-state state- nil ev-)) o events))))

(defmethod next-state* [:ev.g/create #{:plain}]
  [state k ev _]
  (let [{:keys [u/uuid]} ev]
    (-> state
        (assoc :g/status :created)
        (assoc :g/host uuid)
        (merge (select-keys ev [:g.time/created])))))

(defmethod next-state* [:ev.g/setup #{:plain}]
  [state k ev _]
  (-> state
      (merge (select-keys ev [:g.time/duration]))))

(defmethod next-state* [:ev.g/open #{:plain}]
  [state k ev _]
  (-> state
      (assoc :g/status :opened)
      (merge (select-keys ev [:g.time/opened]))))

(defmethod next-state* [:ev.g/close #{:plain}]
  [state k ev _]
  (-> state
      (assoc :g/status :closed)
      (merge (select-keys ev [:g.time/closed]))))

(defmethod next-state* [:ev.g/start #{:plain}]
  [state k ev _]
  (-> state
      (assoc :g/status :started)
      (merge (select-keys ev [:g.time/started]))))

(defmethod next-state* [:ev.g/finish #{:plain}]
  [state k ev _]
  (-> state
      (assoc :g/status :finished)
      (merge (select-keys ev [:g.time/finished]))))

(defmethod next-state* [:ev.g/join #{:plain}]
  [state k ev _]
  (let [{:keys [u/uuid]} ev]
    (-> state
        (update-in [:g/participants] assoc uuid :observer))))

(defmethod next-state* [:ev.g/leave #{:plain}]
  [state k ev _]
  (let [{:keys [u/uuid]} ev]
    (-> state
        (update-in [:g/participants] dissoc uuid))))

(defmethod next-state* [:ev.g/select-role #{:plain}]
  [state k ev _]
  (let [{:keys [u/uuid g/role]} ev]
    (-> state
        (update-in [:g/participants] assoc uuid role))))



(defn make-channels
  []
  (let [ch-game (chan (sliding-buffer 10))
        ch-game-events (chan (sliding-buffer 10))
        ch-inputs (chan (sliding-buffer 10))]
    {:ch-game ch-game
     :ch-game-events ch-game-events
     :ch-inputs ch-inputs}))

#?(:cljs (defn make-store
           ([]
            (make-store {}))
           ([opts]
            (let [state (make-state opts)
                  state* (r/atom (dissoc state :ra.g/state :g/events :db/ds))]
              (merge
               state
               {:ra.g/state state*
                :db/ds nil})))))

;for repl only
(defonce ^:private -store nil)
(defonce ^:private -channels nil)
#?(:cljs (defn proc-game
           [{:keys [ch-game ch-game-events ch-inputs] :as channels} store-arg]
           (set! -store store-arg)
           (set! -channels channels)
           (let []
             (go (loop [store store-arg]
                   (if-let [[v port] (alts! [ch-game-events ch-inputs])]
                     (condp = port
                       ch-game-events (let [store- (next-state store nil v
                                                               '([:ev/event #{:plain}] #{:plain} [:ev/event #{:derived}]))]
                                        (set! -store store-)
                                        (recur store-))
                       ch-inputs (let []
                                   (println v)
                                   (recur store)))))
                 (println "proc-game closing")))))

(comment

  (def store (make-store {}))
  (next-state store nil {:ev/type :ev.g/create}
              '([:ev/event #{:plain}] #{:plain} [:ev/event #{:derived}]))

  
  (def guuid (-> -store :g/uuid))
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
           uuid* (r/cursor (ratoms :ra.g/state) [:g/uuid])
           status* (r/cursor (ratoms :ra.g/state) [:g/status])]
       (fn [_ _]
         (let [uuid @uuid*
               status @status*]
           [:<>
            [:div "rc-game"]
            [:div  uuid]
            [:div  status]]
           )))))

(s/def :g.e/uuid uuid?)
(s/def :g.e/pos (s/tuple int? int?))
(s/def :g.e/type keyword?)
(s/def :g.e.type/cape (s/keys :req [:g.e/type
                                    :g.e/uuid
                                    :g.e/pos]))
(s/def :g.e.type/finding (s/keys :req [:g.e/type
                                       :g.e/uuid
                                       :g.e/pos]))


(defn gen-entities
  "A template: given opts, generates a set of entities for the map"
  [opts]
  
  )

(defn gen-positions
  [x y]
  (->> (for [x (range 0 x)
            y (range 0 y)]
        [[x y] [x y]])
      (into {})))

(comment

  

  ;;
  )