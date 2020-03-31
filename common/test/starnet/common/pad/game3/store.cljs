(ns starnet.common.alpha.game.store
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [cljs.reader :refer [read-string]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.core :refer [make-inst with-gen-fmap]]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]
   [starnet.common.alpha.game.state :refer [ next-state make-state makef-event-tags-recursion]]
   [starnet.common.alpha.game.data :refer [make-entities]]
   [reagent.core :as r]))

(declare update-store)

(defn make-channels
  []
  (let [ch-game (chan 10)
        ch-game-events (chan 100)
        ch-inputs (chan 100)
        ch-worker (chan 100)]
    {:ch-game ch-game
     :ch-game-events ch-game-events
     :ch-inputs ch-inputs
     :ch-worker ch-worker}))

(defn make-store
  ([]
   (make-store {}))
  ([{:keys [channels] :as opts}]
   (let [state (make-state opts)
         state* (r/atom state)
         map* (r/atom {:m/status :initial
                       :m/entities [1]})
         entities* (r/cursor map* [:m/entities])
         count-entities* (r/track! (fn []
                                     (let [xs @entities*]
                                       (when xs
                                         (count xs)))))]
     (merge
      state
      {:ra.g/state state*
       :ra.g/map map*
       :ra.g/entities entities*
       :ra.g/count-entities count-entities*
       :db/ds nil
       :channels channels}))))

(defn chan?
  [x]
  (isa? (type x) cljs.core.async.impl.channels/ManyToManyChannel))

;for repl only
(defonce ^:private -store nil)
(defonce ^:private -channels nil)
(defn proc-store
  [{:keys [ch-game-events ch-inputs] :as channels} store-arg]
  (set! -store store-arg)
  (set! -channels channels)
  (let []
    (go (loop [store store-arg]
          (if-let [[v port] (alts! [ch-game-events ch-inputs])]
            (condp = port
              ch-game-events (let [store- (next-state store nil v
                                                      '([:ev/event #{:plain}]
                                                        #{:plain}))
                                   x (update-store store- nil v
                                                   '([:ev/event #{:derived}]
                                                     #{:derived}))
                                   store-  (if (chan? x) (<! x) x)]
                               (set! -store store-)
                               (recur store-))
              ch-inputs (let []
                          (println v)
                          (recur store)))))
        (println "proc-game closing"))))


(comment

  
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

  (do
    (next-state -store nil {:ev/type :ev.g/start
                            :g/uuid guuid
                            :u/uuid (:u/uuid u1)}
                '([:ev/event #{:plain}]
                  #{:plain}
                  [:ev/event #{:derived}]
                  #{:derived}))
    nil)

  ;;
  )

(defmulti update-store*
  {:arglists '([state key event dispatch-v])}
  (fn [state k ev dispatch-v] dispatch-v))

(defmethod update-store* [:ev/event #{:derived}]
  [state k ev _]
  (let []
    (swap! (:ra.g/state state) merge (dissoc state :ra.g/state :g/events :db/ds :ra.g/map :channels))
    state))

(defmethod update-store* [:ev.g/start #{:derived}]
  [state k ev _]
  (let [{:keys [u/uuid]} ev
        {:keys [ch-worker]} (:channels state)
        map* (:ra.g/map state)]
    (swap! map* assoc :m/status :generating/entities)
    (go
      (let [c-out (chan 1)]
        (>! (-channels :ch-worker) {:worker/op :starnet.common.alpha.game.data/make-entities
                                    :worker/args [{}]
                                    :ch/c-out c-out})
        (let [o (<! c-out)]
          (swap! map* assoc :m/entities o)
          (swap! map* assoc :m/status :done)))
      state)))

(defmethod update-store* :default
  [state k ev dispatch-v]
  #_(println (format "; warning: next-state* :default invoked %s %s" (:ev/type ev) dispatch-v))
  state)


(def update-store (makef-event-tags-recursion update-store*))

; repl only
(def ^:private -worker nil)

(defn proc-worker
  [{:keys [ch-worker] :as channels}]
  (let [worker (js/Worker. "/js-out/worker.js")
        queue (chan 10)]
    (aset worker "onmessage" (fn [e]
                               (take! queue (fn [c]
                                              (put! c (read-string (.-data e)))))))
    (set! -worker worker)
    (go (loop []
          (if-let [v (<! ch-worker)]
            (let [{:keys [ch/c-out]} v]
              (.postMessage worker (pr-str (dissoc v :ch/c-out)))
              (put! queue c-out)
              (recur))))
        (println "proc-worker closing"))))


(comment

  (go
    (let [c (chan 1)]
      (>! (-channels :ch-worker) {:worker/op :b
                                  :ch/c-out c})
      (let [o (<! c)]
        (println "asd" o))))
  
  (go
    (let [c (chan 1)]
      (>! (-channels :ch-worker) {:worker/op :starnet.common.alpha.game.data/make-entities
                                  :ch/c-out c})
      (let [o (<! c)]
        (println "asd" (count o)))))
  
  
  ;;
  )