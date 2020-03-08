(ns starnet.app.kafka.serde-compare
  (:require
   [clojure.pprint :as pp])
  (:import
   starnet.app.alpha.aux.serdes.TransitJsonSerializer
   starnet.app.alpha.aux.serdes.TransitJsonDeserializer
   starnet.app.alpha.aux.serdes.TransitJsonSerde

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

   org.apache.kafka.streams.kstream.Materialized
   org.apache.kafka.streams.kstream.Produced
   org.apache.kafka.streams.kstream.Reducer
   org.apache.kafka.streams.kstream.Grouped
   org.apache.kafka.streams.state.QueryableStoreTypes

   org.apache.kafka.streams.kstream.Initializer
   org.apache.kafka.streams.kstream.Aggregator

   java.util.ArrayList
   java.util.Locale
   java.util.Arrays))


(defn create-topics
  [{:keys [conf
           names
           num-partitions
           replication-factor] :as opts}]
  (let [client (KafkaAdminClient/create conf)
        topics (java.util.ArrayList.
                (mapv (fn [name]
                        (NewTopic. name num-partitions (short replication-factor))) names))]
    (.createTopics client topics)))

(defn delete-topics
  [{:keys [conf
           names] :as opts}]
  (let [client  (KafkaAdminClient/create conf)]
    (.deleteTopics client (java.util.ArrayList. names))))

(defn list-topics
  [{:keys [conf] :as opts}]
  (let [client (KafkaAdminClient/create conf)
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
                              (println "; closing" (.get props "application.id"))
                              (.close streams))
                            (.countDown latch))))))

(defn create-streams-app
  [{:keys [app-id
           topic-in
           topic-out
           key-serde
           value-serde
           key-serde-str
           value-serde-str] :as opts}]
  (let [builder (StreamsBuilder.)
        ktable (-> builder
                   (.stream topic-in)
                   (.groupByKey)
                   (.aggregate (reify Initializer
                                 (apply [this]
                                   nil))
                               (reify Aggregator
                                 (apply [this k v ag]
                                   v))
                               (-> (Materialized/as (str app-id ".store"))
                                   (.withKeySerde key-serde)
                                   (.withValueSerde value-serde)))
                   (.toStream)
                   (.to topic-out))
        topology (.build builder)
        props (doto (Properties.)
                (.putAll {"application.id" app-id
                          "bootstrap.servers" "broker1:9092"
                          "auto.offset.reset" "earliest" #_"latest"
                          "default.key.serde" key-serde-str
                          "default.value.serde" value-serde-str}))
        streams (KafkaStreams. topology props)
        latch (CountDownLatch. 1)]
    (do
      (add-shutdown-hook props streams latch)
      (.cleanUp streams)
      #_(.start streams))
    {:builder builder
     :ktable ktable
     :topology topology
     :props props
     :streams streams
     :latch latch}))


(defn future-call-consumer
  [{:keys [topic
           key-des
           value-des] :as opts}]
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
             (println ";")
             (println (.key rec))
             (println (.value rec)))))))))

(def base-conf {"bootstrap.servers" "broker1:9092"})

(comment

  (def topic-names ["example.serde-compare.string.in"
                    "example.serde-compare.string.out"
                    "example.serde-compare.transit.in"
                    "example.serde-compare.transit.out"])

  (create-topics {:conf base-conf
                  :names topic-names
                  :num-partitions 1
                  :replication-factor 1})

  (list-topics {:conf base-conf})

  (delete-topics {:conf base-conf
                  :names topic-names})

  (def users {0 #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              1 #uuid "179c265a-7f72-4225-a785-2d048d575854"
              2 #uuid "3a3e2d06-3719-4811-afec-0dffdec35543"})

  ; string

  (def app1 (create-streams-app {:app-id "example.serde-compare.string.streams"
                                 :topic-in "example.serde-compare.string.in"
                                 :topic-out "example.serde-compare.string.out"
                                 :key-serde (Serdes/String)
                                 :value-serde (Serdes/String)
                                 :key-serde-str (.. Serdes String getClass)
                                 :value-serde-str (.. Serdes String getClass)}))
  (def streams1 (:streams app1))
  (.isRunning (.state streams1))
  (.start streams1)
  (.close streams1)

  (def consumer-fu1 (future-call-consumer {:topic "example.serde-compare.string.out"
                                           :key-des "org.apache.kafka.common.serialization.StringDeserializer"
                                           :value-des "org.apache.kafka.common.serialization.StringDeserializer"}))

  (future-cancel consumer-fu1)

  (def producer1 (KafkaProducer.
                  {"bootstrap.servers" "broker1:9092"
                   "auto.commit.enable" "true"
                   "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                   "value.serializer" "org.apache.kafka.common.serialization.StringSerializer"}))

  (.send producer1 (ProducerRecord.
                    "example.serde-compare.string.in"
                    (-> (get users 0) (.toString))
                    (str {:a 12})))

  (def store1 (.store streams1 "example.serde-compare.string.streams.store" (QueryableStoreTypes/keyValueStore)))
  (doseq [x (iterator-seq (.all store1))]
    (println (.key x) (.value x)))


  ; transit

  (def app2 (create-streams-app {:app-id "example.serde-compare.transit.streams"
                                 :topic-in "example.serde-compare.transit.in"
                                 :topic-out "example.serde-compare.transit.out"
                                 :key-serde (TransitJsonSerde.)
                                 :value-serde (TransitJsonSerde.)
                                 :key-serde-str "starnet.app.alpha.aux.serdes.TransitJsonSerde"
                                 :value-serde-str "starnet.app.alpha.aux.serdes.TransitJsonSerde"}))

  (def streams2 (:streams app2))
  (.isRunning (.state streams2))
  (.start streams2)
  (.close streams2)

  (def consumer-fu2 (future-call-consumer {:topic "example.serde-compare.transit.out"
                                           :key-des "starnet.app.alpha.aux.serdes.TransitJsonDeserializer"
                                           :value-des "starnet.app.alpha.aux.serdes.TransitJsonDeserializer"}))

  (future-cancel consumer-fu2)

  (def producer2 (KafkaProducer.
                  {"bootstrap.servers" "broker1:9092"
                   "auto.commit.enable" "true"
                   "key.serializer" "starnet.app.alpha.aux.serdes.TransitJsonSerializer"
                   "value.serializer" "starnet.app.alpha.aux.serdes.TransitJsonSerializer"}))

  (.send producer2 (ProducerRecord.
                    "example.serde-compare.transit.in"
                    (get users 0)
                    #{12}))

  (def store2 (.store streams2 "example.serde-compare.transit.streams.store" (QueryableStoreTypes/keyValueStore)))
  (doseq [x (iterator-seq (.all store2))]
    (println (.key x) (.value x)))

  ;;
  )
