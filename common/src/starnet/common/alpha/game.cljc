(ns starnet.common.alpha.game
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.core :refer [make-inst with-gen-fmap]]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]))

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
