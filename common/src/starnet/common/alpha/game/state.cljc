(ns starnet.common.alpha.game.state
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
   
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]))

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

  (ns-unmap *ns* 'next-state*)

  (next-state nil nil {:ev/type :ev.g/create} '([:ev/event #{:plain}] #{:plain}))

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
    (swap! (:ra.g/state state) merge (dissoc state :ra.g/state :g/events :db/ds :ra.g/map))
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
