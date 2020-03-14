(ns starnet.app.alpha.main
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub]]
   [starnet.app.alpha.aux.nrepl :refer [start-nrepl-server]]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]

   [starnet.app.alpha.aux.serdes]

   [starnet.common.alpha.spec]
   [starnet.app.alpha.spec]
   [starnet.common.pad.all]

   [starnet.app.alpha.repl]
   [starnet.app.alpha.tests]
   [starnet.app.alpha.crux]
   [starnet.app.alpha.http]
   [starnet.app.crux-samples.core]

   [starnet.app.alpha.streams :refer [create-topics list-topics
                                      delete-topics produce-event
                                      future-call-consumer read-store
                                      send-event create-streams-game create-streams-user]]
   [starnet.app.alpha.http  :as app-http])
  (:import
   org.apache.kafka.common.KafkaFuture$BiConsumer))

(declare env-optimized? proc-main proc-http-server
         proc-derived-1 proc-topics proc-streams)

(def chan-main (chan 1))
(def chan-system (chan (a/sliding-buffer 10)))
(def chan-system-pub (pub chan-system first))
(def derived-1 (atom {}))

(defn -main  [& args]
  (proc-derived-1  chan-system-pub derived-1)
  (proc-topics chan-system-pub chan-system)
  (proc-streams chan-system-pub chan-system)
  (proc-http-server chan-system-pub chan-system)
  (put! chan-system [:http-server :start])
  (put! chan-main :start)
  (<!! (proc-main chan-main)))

(defn env-optimized?
  []
  (let [appenv (read-string (System/getenv "appenv"))]
    (:optimized appenv)))

(defn proc-main
  [c]
  (go (loop [nrepl-server nil]
        (when-let [v (<! c)]
          (condp = v
            :start (let [sr (start-nrepl-server "0.0.0.0" 7788)]
                     (when-not (env-optimized?)
                       (stest/instrument)
                       (s/check-asserts true))
                     (when (env-optimized?)
                       (alter-var-root #'clojure.test/*load-tests* (fn [_] false)))
                     (recur sr))
            :stop (recur nrepl-server)
            :exit (System/exit 0))))
      (println "closing proc-main")))

(comment

  (stest/unstrument)

  (put! chan-main :exit)

  ;;
  )

(defn proc-http-server
  [p out]
  (let [c (chan 1)]
    (sub p :http-server c)
    (go (loop [server nil]
          (when-let [[t v] (<! c)]
            (condp = v
              :start (let [sr (app-http/-main-dev)]
                       (recur sr))
              :stop (recur server))))
        (println "closing proc-http-server"))))


(def props {"bootstrap.servers" "broker1:9092"})

(def topics ["alpha.user"
             "alpha.user.changes"
             "alpha.game"
             "alpha.game.changes"])

(defn proc-topics
  [p out]
  (let [c (chan 1)]
    (sub p :ktopics c)
    (go (loop []
          (when-let [[t v] (<! c)]
            (prn [t v])
            (condp = v
              :create (do
                        (-> (create-topics {:props props
                                            :names topics
                                            :num-partitions 1
                                            :replication-factor 1})
                            (.all)
                            (.whenComplete
                             (reify KafkaFuture$BiConsumer
                               (accept [this res err]
                                 (println "topics created")
                                 (>! out [:ktopics-created res]))))))
              :delete (delete-topics {:props props :names topics})))
          (recur))
        (println "proc-topics exiting"))
    c))


(comment

  (-> (create-topics {:props props
                      :names topics
                      :num-partitions 1
                      :replication-factor 1})
      (.all)
      (.whenComplete
       (reify KafkaFuture$BiConsumer
         (accept [this res err]
           (println "; created topics " topics)))))

  (delete-topics {:props props :names topics})
  (list-topics {:props props})
  

  ;;
  )

(defn proc-streams
  [p out]
  (let [c (chan 1)]
    (sub p :kstreams c)
    (go (loop [app-state nil]
          (when-let [[t [k args]] (<! c)]
            (condp = k
              :create (let [{:keys [create id]} args
                            app (create)]
                        (>! out [:kv [id app]])
                        (recur app))
              :close (do
                       (.close (:kstreams app-state))
                       (recur app-state))
              :start (do (.start (:kstreams app-state))
                         (recur app-state))
              :cleanup (do (.cleanUp (:kstreams app-state))
                           (recur app-state)))))
        (println "proc-streams exiting"))
    c))

(defn proc-derived-1
  [p derived]
  (let [c (chan 1)]
    (sub p :kv c)
    (go (loop []
          (when-let [[t [k v]] (<! c)]
            (do
              (swap! derived assoc k v)))
          (recur))
        (println "proc-view exiting"))
    c))

(comment

  (put! sys-chan-1 [:ktopics :create])
  (list-topics {:props props})
  (put! sys-chan-1 [:ktopics :delete])
  (list-topics {:props props})

  (put! sys-chan-1 [:kstreams [:create {:create create-streams-user
                                        :id :create-streams-user}]])

  @view-1
  (def streams-user (-> @view-1 :create-streams-user :kstreams))
  (.isRunning (.state streams-user))
  (put! sys-chan-1 [:kstreams [:start {}]])
  (put! sys-chan-1 [:kstreams [:close {}]])

  ;;
  )

