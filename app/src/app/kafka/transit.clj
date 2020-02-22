(ns app.kafka.transit
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
   java.util.ArrayList
   java.util.Locale
   java.util.Arrays))

; create topic for entities
; aggregate data into view(s)
; query to list entites and their current state

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

(def base-conf {"bootstrap.servers" "broker1:9092"})

(comment

  (create-topic {:conf base-conf
                 :name "transit-input"
                 :num-partitions 1
                 :replication-factor 1})

  (list-topics {:conf base-conf})

  (delete-topics {:conf base-conf
                  :names ["transit-input"]})

  (def fu-consumer
    (future-call (fn []
                   (let [consumer (KafkaConsumer.
                                   {"bootstrap.servers" "broker1:9092"
                                    "auto.offset.reset" "earliest"
                                    "auto.commit.enable" "false"
                                    "group.id" (.toString (java.util.UUID/randomUUID))
                                    "consumer.timeout.ms" "5000"
                                    "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"
                                    "value.deserializer" "app.kafka.serdes.TransitJsonDeserializer"})]
                     (.subscribe consumer (Arrays/asList (object-array ["transit-input"])))
                     (while true
                       (let [records (.poll consumer 1000)]
                         (.println System/out (str "polling records:" (java.time.LocalTime/now)))
                         (doseq [rec records]
                           (prn (str (.key rec) " : " (.value rec))))))))))
  
  (future-cancel fu-consumer)

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))

  (.send producer (ProducerRecord.
                   "transit-input"
                   (.toString (java.util.UUID/randomUUID)) {:a 1}))

  ;
  )

