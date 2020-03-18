(ns starnet.app.alpha.streams
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub sliding-buffer]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]

   [starnet.common.alpha.spec :refer [event-to-topic event-to-recordkey]]
   [starnet.common.alpha.game :refer [next-state-game]]
   [starnet.common.alpha.user :refer [next-state-user]])
  (:import
   starnet.app.alpha.aux.serdes.TransitJsonSerializer
   starnet.app.alpha.aux.serdes.TransitJsonDeserializer
   starnet.app.alpha.aux.serdes.TransitJsonSerde
   starnet.app.alpha.aux.serdes.NippySerde

   org.apache.kafka.common.serialization.Serdes
   org.apache.kafka.streams.KafkaStreams
   org.apache.kafka.streams.StreamsBuilder
   org.apache.kafka.streams.StreamsConfig
   org.apache.kafka.streams.Topology
   org.apache.kafka.streams.kstream.KStream
   org.apache.kafka.streams.kstream.KTable
   java.util.Properties
   java.util.concurrent.CountDownLatch
   org.apache.kafka.clients.admin.KafkaAdminClient
   org.apache.kafka.clients.admin.NewTopic
   org.apache.kafka.clients.consumer.KafkaConsumer
   org.apache.kafka.clients.producer.KafkaProducer
   org.apache.kafka.clients.producer.ProducerRecord
   org.apache.kafka.streams.kstream.ValueMapper
   org.apache.kafka.streams.kstream.KeyValueMapper
   org.apache.kafka.streams.KeyValue
   org.apache.kafka.streams.kstream.Consumed

   org.apache.kafka.streams.KafkaStreams$StateListener


   org.apache.kafka.streams.kstream.Materialized
   org.apache.kafka.streams.kstream.Produced
   org.apache.kafka.streams.kstream.Reducer
   org.apache.kafka.streams.kstream.Grouped
   org.apache.kafka.streams.state.QueryableStoreTypes

   org.apache.kafka.streams.kstream.Initializer
   org.apache.kafka.streams.kstream.Aggregator
   org.apache.kafka.common.KafkaFuture$BiConsumer
   org.apache.kafka.streams.KafkaStreams$State
   org.apache.kafka.streams.kstream.Predicate
   org.apache.kafka.streams.kstream.ValueJoiner

   java.util.ArrayList
   java.util.Locale
   java.util.Arrays))

(defn create-topics
  [{:keys [props
           names
           num-partitions
           replication-factor] :as opts}]
  (let [client (KafkaAdminClient/create props)
        topics (java.util.ArrayList.
                (mapv (fn [name]
                        (NewTopic. name num-partitions (short replication-factor))) names))]
    (.createTopics client topics)))

(defn create-topics-async
  [kprops ktopics]
  (let [cout (chan 1)]
    (go
      (-> (create-topics {:props kprops
                          :names ktopics
                          :num-partitions 1
                          :replication-factor 1})
          (.all)
          (.whenComplete
           (reify KafkaFuture$BiConsumer
             (accept [this res err]
               (println "topics created")
               (>! cout res))))))
    cout))

(defn create-kvstore
  [kstreams name]
  (.store kstreams
          name
          (QueryableStoreTypes/keyValueStore)))

(defn create-store-async-TMP
  [kstreams name]
  (let [dur 3000
        t (timeout dur)]
    (go (loop []
          (if-let [[v port] (alts! [(timeout 300) t])]
            (cond
              (.isRunning kstreams) (create-kvstore kstreams name)
              (= port t) (throw (ex-info (format "Could not create kstore within %s ms" 3000)
                                         {:kstreams kstreams
                                          :name name}))
              :else (recur)))))))

(defn delete-topics
  [{:keys [props
           names] :as opts}]
  (let [client  (KafkaAdminClient/create props)]
    (.deleteTopics client (java.util.ArrayList. names))))

(defn list-topics
  [{:keys [props] :as opts}]
  (let [client (KafkaAdminClient/create props)
        kfu (.listTopics client)]
    (.. kfu (names) (get))))

(defn add-shutdown-hook
  [props streams latch]
  (-> (Runtime/getRuntime)
      (.addShutdownHook (proxy
                         [Thread]
                         ["streams-shutdown-hook"]
                          (run []
                            (when (.isRunning (.state streams))
                              (.println (System/out))
                              (println (format "; %s streams-shutdown-hook" (.get props "application.id")))
                              (.close streams))
                            (.countDown latch))))))

(defn produce-event
  [producer topic k event]
  (.send producer (ProducerRecord.
                   topic
                   k
                   event)))



(defn read-store-to-lzseq
  "Returns a lzseq of kafka KeyValue from kafka store"
  [store f]
  (with-open [it (.all store)]
    (let [sqn (iterator-seq (.all store))]
      (f sqn))))

(defn read-store
  "Returns a vector or map of [key value] from kafka ro-store"
  [store & {:keys [offset limit intomap? fval fkey]
            :or {offset 0
                 limit ##Inf
                         intomap? false
                 fval identity
                 fkey identity}}]
  (cond->> (read-store-to-lzseq store (fn [sqn]
                                        (->> sqn
                                             (drop offset)
                                             (take limit))))
    true (mapv (fn [kv]
                 [(fkey (.key kv)) (fval (.value kv))]))
    intomap? (into {})))

(defn future-call-consumer
  [{:keys [topic
           key-des
           value-des
           recordf]
    :or {key-des "starnet.app.alpha.aux.serdes.TransitJsonDeserializer"
         value-des "starnet.app.alpha.aux.serdes.TransitJsonDeserializer"}
    :as opts}]
  (future-call
   (fn []
     (let [consumer (KafkaConsumer.
                     {"bootstrap.servers" "broker1:9092"
                      "auto.offset.reset" "earliest"
                      "auto.commit.enable" "false"
                      "group.id" (.toString (java.util.UUID/randomUUID))
                      "consumer.timeout.ms" "5000"
                      "key.deserializer" key-des
                      "value.deserializer" value-des})]
       (.subscribe consumer (Arrays/asList (object-array [topic])))
       (while true
         (let [records (.poll consumer 1000)]
           (.println System/out (str "; polling " topic (java.time.LocalTime/now)))
           (doseq [rec records]
             (recordf rec))))))))

(defn create-streams
  [props topology-fn]
  (let [topology (topology-fn)
        props (doto (Properties.)
                (.putAll props ))
        kstreams (KafkaStreams. topology props)
        latch (CountDownLatch. 1)
        ch-state (chan (sliding-buffer 1))
        ch-running (chan (sliding-buffer 1))]
    (do
      (add-shutdown-hook props kstreams latch)
      #_(.setStateListener kstreams (reify KafkaStreams$StateListener
                                      (onChange
                                        [_ nw old]
                                        (let [running? (= KafkaStreams$State/RUNNING nw)
                                              v {:ch/topic appid
                                                 :kafka/running? running?
                                                 :kafka/new-state nw
                                                 :kafka/old-state old
                                                 :kafka/kstreams kstreams}]
                                          (put! ch-state v)
                                          (when running?
                                            (put! ch-running v))
                                          (println (format "; %s %s" appid (.name nw))))))))
    {:appid (get props "application.id")
     :topology topology
     :props props
     :kstreams kstreams
     :latch latch
     :ch-state ch-state
     :ch-running ch-running}))

(defmulti send-event
  "Send kafka event. Topic is mapped by ev/type."
  {:arglists '([kproducer ev]
               [kproducer ev topic]
               [kproducer ev uuidkey]
               [kproducer ev topic uuidkey])}
  (fn [kproducer ev & args]
    (mapv type (into [ev] args))))

(defmethod send-event [:isa/kproducer Object]
  [kproducer ev ]
  (.send kproducer
         (ProducerRecord.
          (event-to-topic ev)
          (event-to-recordkey ev)
          ev)))

(defmethod send-event [:isa/kproducer Object String ]
  [kproducer ev topic]
  (.send kproducer
         (ProducerRecord.
          topic
          (event-to-recordkey ev)
          ev)))

(defmethod send-event [:isa/kproducer Object :isa/uuid ]
  [kproducer ev uuidkey ]
  (.send kproducer
         (ProducerRecord.
          (event-to-topic ev)
          uuidkey
          ev)))

(defmethod send-event [:isa/kproducer Object String :isa/uuid]
  [kproducer ev topic uuidkey]
  (.send kproducer
         (ProducerRecord.
          topic
          uuidkey
          ev)))

(defmulti next-state-kstreams-access
  (fn [_ k ev ag]
    (:ev/type ev)))
(defmethod next-state-kstreams-access :ev.access/create
  [_ k ev ag]
  (:access/record ev))
(defmethod next-state-kstreams-access :ev.access/delete
  [_ k ev ag]
  nil)

(defn create-kstreams-access
  []
  (create-streams
   {"application.id" "alpha.access.streams"
    "bootstrap.servers" "broker1:9092"
    "auto.offset.reset" "earliest" #_"latest"
    "default.key.serde" "starnet.app.alpha.aux.serdes.TransitJsonSerde"
    "default.value.serde" "starnet.app.alpha.aux.serdes.TransitJsonSerde"}
   (fn []
     (let [builder0 (StreamsBuilder.)
           kstream0 (-> builder0
                        (.stream "crux-docs"
                                 (Consumed/with (Serdes/String) (NippySerde.)))
                        (.filter (reify Predicate
                                   (test [_ k v]
                                     (contains? v :u/uuid))))
                        (.groupBy (reify KeyValueMapper
                                    (apply [_ k v]
                                      (let [k (:u/uuid v)]
                                        #_(KeyValue. k v)
                                        k)))
                                  (Grouped/with
                                   (TransitJsonSerde.) (TransitJsonSerde.)))
                        (.reduce (reify Reducer
                                   (apply [_ v1 v2]
                                     (println "vals")
                                     (println v1)
                                     (println v2)
                                     v2))
                                 (-> (Materialized/as "alpha.access.streams.user-store1")
                                     (.withKeySerde (TransitJsonSerde.))
                                     (.withValueSerde (TransitJsonSerde.)))))

           builder (StreamsBuilder.)
           kstream (-> builder
                       (.stream "alpha.token")
                       (.groupByKey)
                       (.aggregate (reify Initializer
                                     (apply [this]
                                       nil))
                                   (reify Aggregator
                                     (apply [this k ev ag]
                                       (apply next-state-kstreams-access [this k ev ag])))
                                   (-> (Materialized/as "alpha.access.streams.store")
                                       (.withKeySerde (Serdes/String))
                                       (.withValueSerde (TransitJsonSerde.)))))]
       (.build builder0)))))


(comment
  
  (def app (create-kstreams-access))
  (def kstream (:kstreams app))
  (.state kstream)
  (.start kstream)
  (.stop kstream)
  
  (def store (.store kstream "alpha.access.streams.user-store1" (QueryableStoreTypes/keyValueStore)))
  (read-store store)
  
  ;;
  )

(defn assert-next-game-post [state] {:post [(s/assert :g/game %)]} state)
(defn assert-next-game-body [state]
  (let [data (s/conform :g/game state)]
    (if (= data ::s/invalid)
      (throw (ex-info "Invalid data"
                      (select-keys (s/explain-data :g/game state) [::s/spec ::s/problems])))
      data)))

(comment

  (def a-game (sgen/generate (s/gen :g/game)))
  (def ev {:ev/type :ev.g.u/configure
           :u/uuid (java.util.UUID/randomUUID)
           :g/uuid (java.util.UUID/randomUUID)
           :g/status :opened})
  (def nv (next-state a-game (java.util.UUID/randomUUID) ev))
  (def anv (assert-next-game-post nv))
  (def anv (assert-next-game-post a-game))
  (def anv (assert-next-game-body nv))
  (s/explain :g/game nv)
  (s/assert :g/game nv)
  (s/assert :g/game a-game)
  (s/check-asserts?)
  (s/check-asserts true)

  (keys (s/explain-data :g/game nv))
  (s/conform :g/game nv)

  (try
    (assert-next-game-body nv)
    (catch Exception e
      #_(println e)
      (println (ex-message e))
      (println (ex-data e))))
  ;;
  )

(defn create-kstreams-game
  []
  (create-streams
   {"application.id" "alpha.game.streams"
    "bootstrap.servers" "broker1:9092"
    "auto.offset.reset" "earliest" #_"latest"
    "default.key.serde" "starnet.app.alpha.aux.serdes.TransitJsonSerde"
    "default.value.serde" "starnet.app.alpha.aux.serdes.TransitJsonSerde"}
   (fn []
     (let [builder (StreamsBuilder.)
           kstream (-> builder
                       (.stream "alpha.game")
                       (.groupByKey)
                       (.aggregate (reify Initializer
                                     (apply [this]
                                       nil))
                                   (reify Aggregator
                                     (apply [this k v ag]
                                       (try
                                         (assert-next-game-body (next-state-game ag k v))
                                         (catch Exception e
                                           (println (ex-message e))
                                           (println (ex-data e))
                                           ag))))
                                   (-> (Materialized/as "alpha.game.streams.store")
                                       (.withKeySerde (TransitJsonSerde.))
                                       (.withValueSerde (TransitJsonSerde.))))
                       (.toStream)
                       (.to "alpha.game.changes"))]
       (.build builder)))))



(defn next-state-user-games [])