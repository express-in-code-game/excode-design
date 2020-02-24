(ns app.kafka.aggregate-delete-example
  (:require [clojure.pprint :as pp])
  (:import
   app.kafka.serdes.TransitJsonSerializer
   app.kafka.serdes.TransitJsonDeserializer
   app.kafka.serdes.TransitJsonSerde

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

(def base-conf {"bootstrap.servers" "broker1:9092"})

(comment

  (def topic-names ["aggregate-delete.example"])

  (create-topics {:conf base-conf
                  :names topic-names
                  :num-partitions 1
                  :replication-factor 1})

  (list-topics {:conf base-conf})

  (delete-topics {:conf base-conf
                  :names topic-names})

  (do
    (def builder (StreamsBuilder.))

    (def ktable (-> builder
                    (.stream "aggregate-delete.example")
                    (.groupByKey)
                    #_(.groupBy (reify KeyValueMapper
                                  (apply [this k v]
                                    (KeyValue/pair k v))))
                    (.aggregate (reify Initializer
                                  (apply [this]
                                    nil))
                                (reify Aggregator
                                  (apply [this k v ag]
                                    (cond
                                      (= v {:delete true}) nil
                                      :else (merge ag v))))
                                (-> (Materialized/as "aggregate-delete.example.streams.store")
                                    (.withKeySerde (Serdes/String))
                                    (.withValueSerde (TransitJsonSerde.))))
                    #_(.reduce (reify Reducer
                                 (apply [this ag v]
                                   (cond
                                     (= v {:delete true}) nil
                                     :else (merge ag v))))
                               (-> (Materialized/as "aggregate-delete.example.streams.store")
                                   (.withKeySerde (Serdes/String))
                                   (.withValueSerde (TransitJsonSerde.))))))

    (def topology (.build builder))

    (println (.describe topology))

    (def streams-props (doto (Properties.)
                         (.putAll {"application.id" "aggregate-delete.example.streams"
                                   "bootstrap.servers" "broker1:9092"
                                   "auto.offset.reset" "earliest"
                                   "default.key.serde" (.. Serdes String getClass)
                                   "default.value.serde" "app.kafka.serdes.TransitJsonSerde"})))


    (def streams (KafkaStreams. topology streams-props))

    (def latch (CountDownLatch. 1))

    (add-shutdown-hook streams-props streams latch)
    ;
    )

  (.start streams)
  (.close streams)
  (.isRunning (.state streams))

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))

  (def users {0 (.toString #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
              1 (.toString #uuid "179c265a-7f72-4225-a785-2d048d575854")
              2 (.toString #uuid "3a3e2d06-3719-4811-afec-0dffdec35543")})

  (.send producer (ProducerRecord.
                   "aggregate-delete.example"
                   (get users 0)
                   {:email "user0@gmail.com"
                    :username "user0"}))

  (.send producer (ProducerRecord.
                   "aggregate-delete.example"
                   (get users 1)
                   {:email "user1@gmail.com"
                    :username "user1"}))

  (.send producer (ProducerRecord.
                   "aggregate-delete.example"
                   (get users 2)
                   {:email "user2@gmail.com"
                    :username "user2"}))

  (.send producer (ProducerRecord.
                   "aggregate-delete.example"
                   (get users 2)
                   {:delete true}))

  (def readonly-store (.store streams "aggregate-delete.example.streams.store" (QueryableStoreTypes/keyValueStore)))

  (.approximateNumEntries readonly-store)
  (count (iterator-seq (.all readonly-store)))
  (doseq [x (iterator-seq (.all readonly-store))]
    (println (.key x) (.value x)))


  (.get readonly-store (get users 0))
  (.get readonly-store (get users 1))
  (.get readonly-store (get users 2))

  (.cleanUp streams)



  ;;
  )