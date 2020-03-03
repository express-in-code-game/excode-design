(ns app.kafka.ktable-agg-example
  (:require [clojure.pprint :as pp]
            [app.kafka.serdes])
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
   org.apache.kafka.streams.kstream.Materialized
   org.apache.kafka.streams.kstream.Produced
   org.apache.kafka.streams.kstream.Grouped
   org.apache.kafka.streams.state.QueryableStoreTypes
   
   org.apache.kafka.streams.kstream.Initializer
   org.apache.kafka.streams.kstream.Aggregator
   
   java.util.ArrayList
   java.util.Locale
   java.util.Arrays))

(defn create-topic
  [{:keys [conf
           name
           num-partitions
           replication-factor] :as opts}]
  (let [client (KafkaAdminClient/create conf)
        topics (java.util.ArrayList.
                [(NewTopic. name num-partitions (short replication-factor))])]
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

  (create-topic {:conf base-conf
                 :name "ktable-agg-example.wordcount"
                 :num-partitions 1
                 :replication-factor 1})

  (list-topics {:conf base-conf})

  (delete-topics {:conf base-conf
                  :names ["ktable-agg-example.wordcount"
                          "ktable-agg-example.wordcount.output"
                          "ktable-agg-example.wordcount-aggregated-stream-store-changelog"]})
  
  (def builder (StreamsBuilder.))

  (def kstream (.stream builder "ktable-agg-example.wordcount"))

  (def kgrouped-stream (.groupByKey kstream (Grouped/with (Serdes/String)  (Serdes/Integer))))

  (def ktable (.aggregate kgrouped-stream
                          (reify Initializer
                            (apply [this]
                              (int 0)))
                          (reify Aggregator
                            (apply [this k v ag]
                              (int (+ ag v))))
                          (-> (Materialized/as "aggregated-stream-store")
                              (.withKeySerde (Serdes/String))
                              (.withValueSerde (Serdes/Integer)))))

  (def topology (.build builder))

  (println (.describe topology))

  (def streams (KafkaStreams.
                topology
                (doto (Properties.)
                  (.putAll {"application.id" "ktable-agg-example.wordcount"
                            "bootstrap.servers" "broker1:9092"
                            "default.key.serde" (.. Serdes String getClass)
                            "default.value.serde" (.. Serdes Integer getClass)}))))

  (def latch (CountDownLatch. 1))

  (add-shutdown-hook streams latch)

  (def fu-streams
    (future-call
     (fn []
       (.start streams)
       #_(.await latch) ; halts
       )))

  (future-cancel fu-streams)
  (.close streams)

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                  "value.serializer" "org.apache.kafka.common.serialization.IntegerSerializer"}))

  (.send producer (ProducerRecord.
                   "ktable-agg-example.wordcount"
                   "hello"
                   (int 1)))

  (.send producer (ProducerRecord.
                   "ktable-agg-example.wordcount"
                   "world"
                   (int 1)))

  (def queryableStoreName (.queryableStoreName ktable))
  (def view (.store streams queryableStoreName (QueryableStoreTypes/keyValueStore)))
  (.get view "hello")
  (.get view "world")


  ;
  )