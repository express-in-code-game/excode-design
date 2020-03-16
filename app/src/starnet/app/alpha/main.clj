(ns starnet.app.alpha.main
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub]]
   [clojure.set :refer [subset?]]
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

   [starnet.app.alpha.streams :refer [create-topics-async list-topics
                                      delete-topics produce-event create-kvstore
                                      future-call-consumer read-store
                                      send-event create-kstreams-game create-kstreams-access]]
   [starnet.app.alpha.http  :as app-http]
   [starnet.app.alpha.crux :as app-crux]
   [crux.api :as crux])
  (:import
   org.apache.kafka.clients.producer.KafkaProducer))

(declare env-optimized? proc-main proc-http-server
         proc-derived-1  proc-streams proc-log
         proc-cruxdb proc-kproducer proc-kstreams-access)

(def ch-main (chan 1))
(def ch-sys (chan (a/sliding-buffer 10)))
(def pub-sys (pub ch-sys first))
(def a-derived-1 (atom {}))
(def ch-db (chan 10))
(def ch-kproducer (chan 10))
(def ch-access-store (chan 10))

(defn -main  [& args]
  (proc-derived-1  pub-sys a-derived-1)
  (proc-streams pub-sys ch-sys)
  (proc-http-server pub-sys)
  (proc-cruxdb pub-sys ch-db)
  (proc-kproducer pub-sys ch-kproducer)
  (proc-kstreams-access pub-sys ch-sys)
  #_(put! ch-sys [:kstreams-access :start])
  #_(put! ch-sys [:kstreams-game :start])
  #_(put! ch-sys [:kproducer :open])
  #_(put! ch-sys [:cruxdb :start])
  #_(put! ch-sys [:http-server :start])
  (put! ch-main :start)
  (<!! (proc-main ch-main)))

(comment
  
  (put! ch-sys [:http-server :start])

  (put! ch-sys [:cruxdb :start])
  (put! ch-sys [:cruxdb :close])
  
  (stest/unstrument)

  (put! ch-main :exit)
  ;;
  )

(defn env-optimized?
  []
  (let [appenv (read-string (System/getenv "appenv"))]
    (:optimized appenv)))

(defn proc-main
  [ch-main]
  (go (loop [nrepl-server nil]
        (when-let [v (<! ch-main)]
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

(defn proc-http-server
  [pub-sys]
  (let [c (chan 1)]
    (sub pub-sys :http-server c)
    (go (loop [server nil]
          (when-let [[_ v] (<! c)]
            (condp = v
              :start (let [sr (app-http/-main-dev)]
                       (recur sr))
              :stop (recur server))))
        (println "closing proc-http-server"))))

(defn proc-log
  [pub-sys]
  (let [c (chan 1)]
    (sub pub-sys :log c)
    (go (loop []
          (if-let [[_ s] (<! c)]
            (println (str "; " s))
            (recur)))
        (println "closing proc-http-server"))))



(def crux-conf {:crux.node/topology '[crux.kafka/topology
                                      crux.kv.rocksdb/kv-store]
                :crux.kafka/bootstrap-servers "broker1:9092"
                :crux.kafka/tx-topic "crux-transaction-log"
                :crux.kafka/doc-topic "crux-docs"
                :crux.kafka/create-topics true
                :crux.kafka/doc-partitions 1
                :crux.kafka/replication-factor (short 1)
                :crux.kv/db-dir "/ctx/data/crux"
                :crux.kv/sync? false
                :crux.kv/check-and-store-index-version true})

(defn proc-cruxdb
  [pub-sys ch-db]
  (let [c (chan 1)]
    (sub pub-sys :cruxdb c)
    (go (loop [node nil]
          (if-let [[vl port] (alts! (if node [c ch-db] [c]))] ; add check if node is valid
            (condp = port
              c (condp = (second vl)
                  :start (let [n (crux/start-node crux-conf)]
                           (alter-var-root #'app-crux/node (constantly n)) ; for dev purposes
                           (println "; crux node started")
                           (recur n))
                  :close (do
                           (.close node)
                           (alter-var-root #'app-crux/node (constantly nil)) ; for dev purposes
                           (println "; crux node closed")
                           (recur nil)))
              ch-db (let [[f args cout] v]
                      (go
                        (let [x (f args)] ; db call here
                          (>! cout x) ; convey data
                          ))
                      (recur node))
              )))
        (println "closing proc-cruxdb"))))

(def kprops-producer {"bootstrap.servers" "broker1:9092"
                      "auto.commit.enable" "true"
                      "key.serializer" "starnet.app.alpha.aux.serdes.TransitJsonSerializer"
                      "value.serializer" "starnet.app.alpha.aux.serdes.TransitJsonSerializer"})

(defn proc-kproducer
  [pub-sys ch-kproducer]
  (let [c (chan 1)]
    (sub pub-sys :kproducer c)
    (go (loop [kproducer nil]
          (if-let [[vl port] (alts! (if kproducer [c ch-kproducer] [c]))]
            (condp = port
              c (condp = (second vl)
                  :open (let [kp (KafkaProducer. kprops-producer)]
                          (println "; kprodcuer created")
                          (recur kp))
                  :close (do
                           (.close kproducer)
                           (println "; kproducer closed")
                           (recur nil)))
              ch-kproducer (let [[args cout] vl]
                             (>! cout (apply send-event kproducer args)) ; may deref future
                             (recur kproducer))))
          ))))

; not used in the system, for repl purposes only
(def ^:private a-kstreams (atom {}))

(defn proc-kstreams-access
  [pub-sys ch-sys]
  (let [csys (chan 1)]
    (sub pub-sys :kstreams-access csys)
    (go (loop [app nil]
          (if-let [[_ vl] (<! csys)]
            (condp = vl
              :start (let [a (create-kstreams-access)]
                       (.start (:kstreams a))
                       (swap! a-kstreams assoc :kstreams-access a) ; for repl purposes
                       (println (str "; :kstreams-access started "))
                       (recur a))
              :close (do (when app
                           (.close (:kstreams app))
                           (println (str "; :kstreams-access closed ")))
                         (recur app ))
              :cleanup (do (.cleanUp (:kstreams app))
                           (recur app ))
              (recur app)))))))

(defn proc-access-store
  [pub-sys ch-sys ch-access-store ch-kproducer]
  (let [csys (chan 1)]
    (sub pub-sys :kstreams csys)
    (go (loop [store nil]
          (if-let [[vl port] (alts! (if store [csys ch-access-store] [csys]))]
            (condp = port
              csys (condp = (second vl)
                     :started (let [[_ _ appid] vl
                                    s (when (= appid "alpha.access.streams")
                                        (.store streams-game
                                                "alpha.access.streams.store"
                                                (QueryableStoreTypes/keyValueStore)))]
                                (recur s))
                     :closed (do (.close store)
                                 (recur nil)))
              ch-access-store (let [[op token cout] vl]
                                (condp = op
                                  :get (do (>! cout (.get k store))
                                           (recur store))
                                  (recur store)))))))))

(def kprops {"bootstrap.servers" "broker1:9092"})

(def ktopics ["alpha.token"
              "alpha.access.changes"
              "alpha.game"
              "alpha.game.changes"])

(comment

  (list-topics {:props kprops})
  (delete-topics {:props kprops :names ktopics})
  
  ;;
  )



(defn proc-kstreams
  [pub-sys ch-sys]
  (let [c (chan 1)]
    (sub pub-sys :kstreams c)
    (go (loop [app-state nil]
          (if-not (subset? (set ktopics) (list-topics {:props kprops}))
            (<! (create-topics-async kprops ktopics)))
          (if-let [[t [k args]] (<! c)]
            (condp = k
              :start (let [{:keys [create-fn appid]} args
                           app ((resolve create-fn))]
                       (swap! a-kstreams assoc appid app) ; for repl purposes
                       (.start (:kstreams app))
                       (println (str "; proc-kstreams started "))
                       (>! ch-sys [:kstreams :started appid])
                       (recur app))
              :close (do (when app-state
                           (.close (:kstreams app-state))
                           (>! ch-sys [:kstreams :closed (:appid app-state)]))
                         (recur app-state))
              :cleanup (do (.cleanUp (:kstreams app-state))
                           (recur app-state))
              (recur app-state))))
        (println (str "proc-kstreams exiting")))
    c))

(defn proc-derived-1
  [pub-sys derived]
  (let [c (chan 1)]
    (sub pub-sys :kv c)
    (go (loop []
          (when-let [[t [k v]] (<! c)]
            (do
              (swap! derived assoc k v)))
          (recur))
        (println "proc-view exiting"))
    c))


