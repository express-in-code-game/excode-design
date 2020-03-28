(ns starnet.ui.alpha.worker
  (:require
   [clojure.repl :refer [doc]]
   [reagent.core :as r]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]

   [starnet.common.alpha.game.data :refer [make-entities]]))

(declare start-worker proc-ops)

(def channels {:ch-ops-in (chan (sliding-buffer 10))})

(defn ^:export main
  []
  #_(start-shared-worker channels)
  (start-worker channels)
  (proc-ops channels))

#_(defn start-shared-worker
    [{:keys [ch-ops-in]}]
    (let [on-connect (fn [e]
                       (js/console.log "shared-worker onconnect")
                       (let [port (aget (.-ports e) 0)]
                         (aset port "onmessage"
                               (fn [e]
                                 (println "; shared worker recieved: " (.-data e))
                                 (let [c (chan 1)]
                                   (put! ch-ops-in {:ev/data (.-data e)
                                                    :ch/c-out c})
                                   (take! c (fn [v]
                                              (println "; shared worker sends: " v)
                                              (.postMessage port v))))))))]
      (aset js/self "onconnect" on-connect)))

(defn start-worker
  [{:keys [ch-ops-in]}]
  (let [on-message (fn [e]
                     (let [c-out (chan 1)]
                       (put! ch-ops-in (merge (cljs.reader/read-string (.-data e))
                                              {:ch/c-out c-out}))
                       (take! c-out (fn [v]
                                      (.postMessage js/self (pr-str v))))))]
    (aset js/self "onmessage" on-message)))


(defn proc-ops
  [{:keys [ch-ops-in]}]
  (go (loop []
        (if-let [v (<! ch-ops-in)]
          (let [{:keys [worker/op ch/c-out]} v]
            (condp = op
              :data/make-entities (let [o (make-entities v)]
                                    (>! c-out o))
              :a (>! c-out {:a (rand-int 10)})
              :b (>! c-out {:b (rand-int 10)}))))
        (recur))))


(comment
  
  ;;
  )