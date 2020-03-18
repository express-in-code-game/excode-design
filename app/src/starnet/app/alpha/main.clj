(ns starnet.app.alpha.main
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub sliding-buffer mix admix unmix]]
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
   [starnet.app.alpha.core :as appcore]
   [crux.api :as crux])
  (:import
   org.apache.kafka.clients.producer.KafkaProducer
   org.apache.kafka.clients.producer.ProducerRecord))

(defn env-optimized?
  []
  (let [appenv (read-string (System/getenv "appenv"))]
    (:optimized appenv)))

(declare  proc-main proc-http-server proc-nrepl
          proc-derived-1  proc-kstreams proc-log proc-access-store
          proc-cruxdb proc-kproducer proc-nrepl-server start-kstreams-access start-kstreams-game)

(def channels (let [ch-proc-main (chan 1)
                    ch-sys (chan (sliding-buffer 10))
                    pb-sys (pub ch-sys :ch/topic (fn [_] (sliding-buffer 10)))
                    ch-cruxdb (chan 10)
                    ch-kproducer (chan 10)
                    ch-access-store (chan 10)
                    ch-kstreams-states (chan (sliding-buffer 10))
                    pb-kstreams-states (pub ch-kstreams-states :ch/topic (fn [_] (sliding-buffer 10)))
                    mx-kstreams-states (a/mix ch-kstreams-states)]
                {:ch-proc-main ch-proc-main
                 :ch-sys ch-sys
                 :pb-sys pb-sys
                 :ch-cruxdb ch-cruxdb
                 :ch-kproducer ch-kproducer
                 :ch-access-store ch-access-store
                 :ch-kstreams-states ch-kstreams-states
                 :pb-kstreams-states pb-kstreams-states
                 :mx-kstreams-states mx-kstreams-states}))

(defn -main  [& args]
  (when-not (env-optimized?)
    (stest/instrument)
    (s/check-asserts true))
  (when (env-optimized?)
    (alter-var-root #'clojure.test/*load-tests* (fn [_] false)))
  (put! (channels :ch-proc-main) {:proc/op :start})
  (<!! (proc-main (select-keys channels [:ch-proc-main :ch-sys]))))

(defn proc-main
  [{:keys [ch-proc-main ch-sys]}]
  (go
    (loop []
      (when-let [{op :proc/op} (<! ch-proc-main)]
        (condp = op
          :start
          (do
            (proc-nrepl-server (select-keys channels [:pb-sys]))
            (proc-http-server (select-keys channels [:pb-sys]) channels)
            (proc-cruxdb (select-keys channels [:pb-sys :ch-cruxdb]))
            (proc-kproducer (select-keys channels [:pb-sys :ch-kproducer]))
            (proc-kstreams (select-keys channels [:pb-sys :ch-sys :mx-kstreams-states]))
            (proc-access-store (select-keys channels [:pb-sys :ch-sys :ch-access-store
                                                      :ch-kproducer :pb-kstreams-states]))

            (put! ch-sys {:ch/topic :nrepl-server :proc/op :start})
            (put! ch-sys {:ch/topic :kproducer :proc/op :start})
            (go
              (let [c-out (chan 1)]
                (put! ch-sys {:ch/topic :cruxdb :proc/op :start :ch/c-out c-out})
                (<! c-out)
                #_(start-kstreams-access (select-keys channels [:ch-sys]))))
            #_(start-kstreams-game (select-keys channels [:ch-sys]))
            #_(put! ch-sys [:kproducer :open])
            #_(put! ch-sys [:http-server :start]))
          :exit (System/exit 0)))
      (recur))
    (println "closing proc-main")))

(comment

  (put! (channels :ch-sys) {:ch/topic :http-server :proc/op :start})



  (stest/unstrument)

  (put! (channels :ch-main) {:proc/op :start})

  (<!! (a/into [] (channels :ch-kstreams-states)))

  ;;
  )

(defn proc-nrepl-server
  [{:keys [pb-sys]}]
  (let [c (chan 1)]
    (sub pb-sys :nrepl-server c)
    (go (loop [server nil]
          (if-let [{op :proc/op} (<! c)]
            (condp = op
              :start (let [sr (start-nrepl-server "0.0.0.0" 7788)]
                       (recur sr)))
            (recur server)))
        (println "closing proc-nrepl-server"))))

(defn proc-http-server
  [{:keys [pb-sys]} channels]
  (let [c (chan 1)]
    (sub pb-sys :http-server c)
    (go (loop [server nil]
          (when-let [{op :proc/op} (<! c)]
            (condp = op
              :start (let [sr (app-http/start-dev channels)]
                       (recur sr))
              :stop (recur server))))
        (println "closing proc-http-server"))))

(defn proc-log
  [{:keys [pb-sys]}]
  (let [c (chan 1)]
    (sub pb-sys :log c)
    (go (loop []
          (if-let [{s :log/str} (<! c)]
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
  [{:keys [pb-sys ch-cruxdb]}]
  (let [c (chan 1)]
    (sub pb-sys :cruxdb c)
    (go (loop [node nil]
          (if-let [[v port] (alts! (if node [c ch-cruxdb] [c]))] ; add check if node is valid
            (condp = port
              c (condp = (:proc/op v)
                  :start (let [{:keys [ch/c-out]} v
                               n (crux/start-node crux-conf)]
                           (alter-var-root #'app-crux/node (constantly n)) ; for dev purposes
                           (>! c-out true)
                           (println "; crux node started")
                           (recur n))
                  :close (do
                           (when node
                             (.close node)
                             (alter-var-root #'app-crux/node (constantly nil))  ; for dev purposes
                             )
                           (println "; crux node closed")
                           (recur nil)))
              ch-cruxdb (let [{:keys [cruxdb/op cruxdb/tx-data ch/c-out cruxdb/query-data]} v]
                          (condp = op
                            :query (go
                                     (let [res (crux/q (crux/db node) query-data)] ;
                                       (>! c-out res)))
                            :tx (go
                                  (let [res (crux/submit-tx node tx-data)]
                                    (>! c-out res))))
                          (recur node)))))
        (println "closing proc-cruxdb"))))


(comment
  (put! (channels :ch-sys) {:ch/topic :cruxdb :proc/op :start})
  (put! (channels :ch-sys) {:ch/topic :cruxdb :proc/op :close})

  (let [c-out (chan 1)]
    (put! (channels :ch-cruxdb) {:cruxdb/op :query
                                 :ch/c-out c-out
                                 :cruxdb/query-data '{:find [element]
                                                      :where [[element :type :element/metal]]}})
    (first (alts!! [c-out (timeout 100)])))

  ;;
  )

(def kprops-producer {"bootstrap.servers" "broker1:9092"
                      "auto.commit.enable" "true"
                      "key.serializer" "starnet.app.alpha.aux.serdes.TransitJsonSerializer"
                      "value.serializer" "starnet.app.alpha.aux.serdes.TransitJsonSerializer"})

(defn proc-kproducer
  [{:keys [pb-sys ch-kproducer]}]
  (let [c (chan 1)]
    (sub pb-sys :kproducer c)
    (go (loop [kproducer nil]
          (if-let [[v port] (alts! (if kproducer [c ch-kproducer] [c]))]
            (condp = port
              c (condp = (:proc/op v)
                  :start (let [kp (KafkaProducer. kprops-producer)]
                          (println "; kprodcuer created")
                          (recur kp))
                  :close (do
                           (.close kproducer)
                           (println "; kproducer closed")
                           (recur nil)))
              ch-kproducer (let [{:kafka/keys [topic k ev]
                                  c-out :ch/c-out} v]
                             (>! c-out (.send kproducer
                                            (ProducerRecord.
                                             topic
                                             k
                                             ev))) ; probably should deref kfuture
                             (recur kproducer))))
          ))))

(defn proc-access-store
  [{:keys [pb-sys ch-sys ch-access-store ch-kproducer pb-kstreams-states]}]
  (let [csys (chan 1)
        cstates (chan 1)
        appid "alpha.access.streams"
        store-name "alpha.access.streams.store"]
    (sub pb-sys :kstreams csys)
    (sub pb-kstreams-states appid cstates)
    (go (loop [store nil]
          (if-let [[v port] (alts! (if store [cstates ch-access-store] [cstates]))]
            (condp = port
              cstates (let [{topic :ch/topic
                             :kafka/keys [kstreams running?]} v]
                        (cond
                          (true? running?) (let [s (create-kvstore kstreams store-name)]
                                             (println (format "; kv-store for %s created" appid))
                                             (recur s))
                          (not running?) (do (when store
                                               (println (format "; kv-store for %s closed" appid)))
                                             (recur store))
                          :else (recur store)))
              ch-access-store (let [{op :kstore/op
                                     k :kafka/k
                                     ev :kafka/ev
                                     c-out :ch/c-out} v]
                                (condp = op
                                  :get (do (>! c-out (.get token store))
                                           (recur store))
                                  :read-store (do (>! c-out (read-store store))
                                                  (recur store))
                                  :delete (let [c (chan 1)]
                                            (>! ch-kproducer {:kafka/topic "alpha.token"
                                                              :kafka/k k
                                                              :kafka/ev {:record/delete? true}
                                                              :ch/c-out c})
                                            (<! c)
                                            (>! c-out true)
                                            (recur store))
                                  :create (let [c (chan 1)]
                                            (>! ch-kproducer {:kafka/topic "alpha.token"
                                                              :kafka/k k
                                                              :kafka/ev ev
                                                              :ch/c-out c})
                                            (<! c)
                                            (>! c-out ev)
                                            (recur store))
                                  (recur store))))))
        (println "proc-access-store exiting"))))



(def kprops {"bootstrap.servers" "broker1:9092"})

(def ktopics ["alpha.token"
              "alpha.game"])

(comment

  (list-topics {:props kprops})
  (delete-topics {:props kprops :names ktopics})
  
  ;;
  )

; not used in the system, for repl purposes only
(def ^:private a-kstreams (atom {}))


(defn proc-kstreams
  [{:keys [pb-sys ch-sys mx-kstreams-states]}]
  (let [c (chan 1)]
    (sub pb-sys :kstreams c)
    (go (loop [app nil]
          (if-let [{:keys [proc/op create-kstreams-f repl-only-key]} (<! c)]
            (do
              (if-not (subset? (set ktopics) (list-topics {:props kprops}))
                (<! (create-topics-async kprops ktopics)))
              (condp = op
                :start (let [a (create-kstreams-f)]
                         (swap! a-kstreams assoc repl-only-key a) ; for repl purposes
                         (.start (:kstreams a))
                         (a/admix mx-kstreams-states (:ch-state a))
                         #_(a/admix mx-kstreams-states (:ch-running a))
                         (recur a))
                :close (do (when app
                             (.close (:kstreams app))
                             (a/unmix mx-kstreams-states (:ch-state app))
                             #_(a/unmix mx-kstreams-states (:ch-running app)))
                           (recur app))
                :cleanup (do (.cleanUp (:kstreams app))
                             (recur app))
                (recur app)))))
        (println (str "proc-kstreams exiting")))
    c))

(defn start-kstreams-access
  [{:keys [ch-sys]}]
  (put! ch-sys {:ch/topic :kstreams
                :proc/op :start
                :create-kstreams-f create-kstreams-access
                :repl-only-key :kstreams-access}))

(defn start-kstreams-game
  [{:keys [ch-sys]}]
  (put! ch-sys {:ch/topic :kstreams
                :proc/op :start
                :create-kstreams-f create-kstreams-game
                :repl-only-key :kstreams-game}))


(defn proc-derived-1
  [{:keys [pb-sys]} derived]
  (let [c (chan 1)]
    (sub pb-sys :kv c)
    (go (loop []
          (when-let [{:keys [k v]} (<! c)]
            (do
              (swap! derived assoc k v)))
          (recur))
        (println "proc-view exiting"))
    c))


