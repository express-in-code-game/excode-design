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
   #?(:cljs [reagent.core :as r])
   ))

(declare next-state next-state-events next-state-derived)

(defn make-game-state
  ([]
   (make-game-state {}))
  ([opts]
   (merge {:g/uuid (gen/generate gen/uuid)
           :g/events []}
          (select-keys opts [:g/events :g/uuid]))))

(defmulti next-state
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-state [:ev/batch]
  [state k ev]
  (let [{:keys [g/events]} ev]
    (as-> state o
      (update o :g/events #(-> % (concat events) (vec)))
      (reduce (fn [agg v] (next-state-derived k v)) o events))))

(defmethod next-state :default
  [state k ev]
  (-> state
      (update :g/events #(-> % (conj ev)))
      (next-state-derived k ev)))

(defmulti next-state-derived
  "Returns the next state of the game."
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-state-derived [:ev.g/create]
  [state k ev]
  (let [{:keys [u/uuid]} ev]
    (-> state
        (update :g.derived/status assoc :created)
        (update :g.derived/host assoc uuid)
        (update-in  [:g.derived/time] (select-keys ev [:g.time/created])))))

(defmethod next-state-derived [:ev.g/setup]
  [state k ev]
  (update-in state [:g.derived/time] (select-keys ev [:g.time/duration])))

(defmethod next-state-derived [:ev.g/open]
  [state k ev]
  (-> state
      (update :g.derived/status assoc :opened)
      (update-in  [:g.derived/time] (select-keys ev [:g.time/opened]))))

(defmethod next-state-derived [:ev.g/close]
  [state k ev]
  (-> state
      (update :g.derived/status assoc :closed)
      (update-in  [:g.derived/time] (select-keys ev [:g.time/closed]))))

(defmethod next-state-derived [:ev.g/start]
  [state k ev]
  (-> state
      (update :g.derived/status assoc :started)
      (update-in  [:g.derived/time] (select-keys ev [:g.time/started]))))

(defmethod next-state-derived [:ev.g/finish]
  [state k ev]
  (-> state
      (update :g.derived/status assoc :finished)
      (update-in  [:g.derived/time] (select-keys ev [:g.time/finished]))))

(defmethod next-state-derived [:ev.g/join]
  [state k ev]
  (let [{:keys [u/uuid]} ev]
    (-> state
        (update-in [:g.derived/roles uuid] assoc :observer))))

(defmethod next-state-derived [:ev.g/leave]
  [state k ev]
  (let [{:keys [u/uuid]} ev]
    (-> state
        (update-in [:g.derived/roles] dissoc uuid))))

(defmethod next-state-derived [:ev.g/select-role]
  [state k ev]
  (let [{:keys [u/uuid g/role]} ev]
    (-> state
        (update-in [:g.derived/roles uuid] assoc role))))


(defmethod next-state-derived :default
  [state k ev]
  state)

(defn make-game-channels
  []
  (let [ch-game (chan (sliding-buffer 10))
        ch-game-events (chan (sliding-buffer 10))
        ch-inputs (chan (sliding-buffer 10))]
    {:ch-game ch-game
     :ch-game-events ch-game-events
     :ch-inputs ch-inputs}))

#?(:cljs (defn make-default-ratoms
           []
           (let [state (r/atom (make-game-state {:g/uuid (gen/generate gen/uuid)}))]
             {:state state})))

;for repl only
(defonce ^:private -ratoms nil)
(defonce ^:private -channels nil)
#?(:cljs (defn proc-game
           [{:keys [ch-game ch-game-events ch-inputs] :as channels} ratoms]
           (let []
             (set! -ratoms ratoms)
             (set! -channels channels)
             (go (loop []
                   (if-let [[v port] (alts! [ch-game-events ch-inputs])]
                     (condp = port
                       ch-game-events (let []
                                        (println v)
                                        (recur)))))
                 (println "proc-game closing")))))

(comment
  
  

  ;;
  )

#?(:cljs
   (defn rc-game
     [channels ratoms]
     (let [{:keys [ch-inputs]} channels
           uuid* (r/cursor (ratoms :state) [:g/uuid])]
       (fn [_ _]
         (let [uuid @uuid*]
           [:div "rc-game" uuid])))))




