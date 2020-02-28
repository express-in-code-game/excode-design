(ns app.alpha.streams.broadcast
  (:require [clojure.pprint :as pp]
            [app.alpha.streams.core :refer [add-shutdown-hook
                                            produce-event
                                            create-user]]
            [clojure.spec.test.alpha :as stest])
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


(def opts {:base-props {"bootstrap.servers" "broker1:9092"}})
(def state* (atom {:consumer nil}))

(defn create-broadcast-consumer
  []
  (let [fu-consumer (future-call
                     (fn []
                       (let [consumer (KafkaConsumer.
                                       {"bootstrap.servers" "broker1:9092"
                                        "auto.offset.reset" "earliest"
                                        "auto.commit.enable" "false"
                                        "group.id" (.toString (java.util.UUID/randomUUID))
                                        "consumer.timeout.ms" "5000"
                                        "key.deserializer"
                                        "app.kafka.serdes.TransitJsonDeserializer"
                                        #_"org.apache.kafka.common.serialization.StringDeserializer"
                                        "value.deserializer" "app.kafka.serdes.TransitJsonDeserializer"})]
                         (.subscribe consumer (Arrays/asList (object-array ["alpha.game.events.changes"])))
                         (while true
                           (let [records (.poll consumer 1000)]
                             (.println System/out (str "; alpha.game.events.changes polling:" (java.time.LocalTime/now)))
                             (doseq [rec records]
                               (println ";")
                               (println (.key rec))
                               (println (.value rec))))))))]
    {:fu-consumer fu-consumer}))

(defn mount
  []
  (let [consumer (create-broadcast-consumer)]
    (swap! state* assoc :consumer consumer)))

(defn unmount
  []
  (when (:consumer @state*)
    (future-cancel (:fu-consumer (:consumer @state*)))
    (swap! state* assoc :consumer nil)))

(comment

  (mount)

  (unmount)

  ;;
  )
