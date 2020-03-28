(ns starnet.common.alpha.game.store
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
   [starnet.common.alpha.game.state :refer [next-state* next-state make-state]]
   [starnet.common.alpha.game.data :refer [make-entities]]
   [reagent.core :as r]))

(defn make-channels
  []
  (let [ch-game (chan (sliding-buffer 10))
        ch-game-events (chan (sliding-buffer 10))
        ch-inputs (chan (sliding-buffer 10))
        ch-worker-in (chan (sliding-buffer 10))
        ch-worker-out (chan (sliding-buffer 10))]
    {:ch-game ch-game
     :ch-game-events ch-game-events
     :ch-inputs ch-inputs
     :ch-worker-in ch-worker-in
     :ch-worker-out ch-worker-out}))

(defn make-store
  ([]
   (make-store {}))
  ([opts]
   (let [state (make-state opts)
         state* (r/atom state)
         map* (r/atom {:m/status :initial})
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
       :db/ds nil}))))

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
                                                        #{:plain}
                                                        [:ev/event #{:derived}]
                                                        #{:derived}))]
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

(defmethod next-state* [:ev.g/start #{:derived}]
  [state k ev _]
  (let [{:keys [u/uuid]} ev
        map* (:ra.g/map state)]
    #_(swap! map* assoc :m/status :generating/entities)
    (r/rswap! map* assoc :m/status :generating/entities)
    #_(do @map*)
    (println "hello")
    #_(println (-> @map* :m/status))
    (go
      (let [xs (make-entities {})]
        (swap! map* assoc :m/entities xs)
        (swap! map* assoc :m/status :done)
        (println "done")))
    (println "end")
    state))

(def ^:private -worker nil)

(defn proc-shared-worker
  [{:keys [ch-worker-in ch-worker-out] :as channels} store-arg]
  (let [worker (js/SharedWorker. "/js-out/worker.js")]
    (aset (.-port worker) "onmessage" (fn [e]
                                        (js/console.log "; msg from sharedworker")
                                        (js/console.log e)))
    (set! -worker worker)
    (go (loop []
          (if-let [[v port] (alts! [ch-worker-out])]
            (condp = port
              ch-worker-out (let []
                              (println "; ch-worker-out " v)
                              (.. worker -port (postMessage "to shared worker"))
                              (recur)))))
        (println "proc-worker closing"))))

(defn proc-worker
  [{:keys [ch-worker-in ch-worker-out] :as channels} store-arg]
  (let [worker (js/Worker. "/js-out/worker.js")]
    (aset worker "onmessage" (fn [e]
                               (js/console.log "; msg from worker")
                               (js/console.log e)))
    (set! -worker worker)
    (go (loop []
          (if-let [[v port] (alts! [ch-worker-out])]
            (condp = port
              ch-worker-out (let []
                              (println "; ch-worker-out " v)
                              (.postMessage worker "to worker")
                              (recur)))))
        (println "proc-worker closing"))))




(comment

  (put! (-channels :ch-worker-out) {})
  (js/console.log -worker)
  (.. -worker  -postMessage)
  (.. -worker -port -onmessage)

  
  
  ;;
  )