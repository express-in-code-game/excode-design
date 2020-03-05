(ns starnet.app.kafka.reduce-example
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
  [streams latch]
  (-> (Runtime/getRuntime)
      (.addShutdownHook (proxy
                         [Thread]
                         ["streams-shutdown-hook"]
                          (run []
                            (.println (System/out) "--closing stream")
                            (.close streams)
                            (.countDown latch))))))

(def base-conf {"bootstrap.servers" "broker1:9092"})

(comment

  (def topic-names ["reduce.example.user.data"])

  (create-topics {:conf base-conf
                  :names topic-names
                  :num-partitions 1
                  :replication-factor 1})

  (list-topics {:conf base-conf})

  (delete-topics {:conf base-conf
                  :names topic-names})

  (def builder (StreamsBuilder.))

  (def ktable (-> builder
                  (.stream "reduce.example.user.data")
                  (.groupByKey)
                  (.reduce (reify
                             Reducer
                             (apply [this ag vl]
                               (merge ag vl)))
                           (-> (Materialized/as "reduce.example.user.data.streams.store")
                               (.withKeySerde (Serdes/String))
                               (.withValueSerde (TransitJsonSerde.))))))

  (def topology (.build builder))

  (println (.describe topology))

  (def streams (KafkaStreams.
                topology
                (doto (Properties.)
                  (.putAll {"application.id" "reduce.example.user.data.streams"
                            "bootstrap.servers" "broker1:9092"
                            "default.key.serde" (.. Serdes String getClass)
                            "default.value.serde" "starnet.app.alpha.aux.serdes.TransitJsonSerde"}))))

  (def latch (CountDownLatch. 1))

  (add-shutdown-hook streams latch)

  (.start streams)
  (.close streams)

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                  "value.serializer" "starnet.app.alpha.aux.serdes.TransitJsonSerializer"}))

  (def users {0 (.toString #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
              1 (.toString #uuid "179c265a-7f72-4225-a785-2d048d575854")
              2 (.toString #uuid "3a3e2d06-3719-4811-afec-0dffdec35543")})

  (.send producer (ProducerRecord.
                   "reduce.example.user.data"
                   (get users 0)
                   {:email "user0@gmail.com"
                    :username "user0"}))

  (.send producer (ProducerRecord.
                   "reduce.example.user.data"
                   (get users 1)
                   {:email "user1@gmail.com"
                    :username "user1"}))

  (.send producer (ProducerRecord.
                   "reduce.example.user.data"
                   (get users 2)
                   {:email "user2@gmail.com"
                    :username "user2"}))

  (def view (.store streams "reduce.example.user.data.streams.store" (QueryableStoreTypes/keyValueStore)))
  (.get view (get users 0))
  (.get view (get users 1))
  (.get view (get users 2))



  ;
  )