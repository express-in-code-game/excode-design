(ns app.alpha.streams.core-test
  (:require [clojure.pprint :as pp]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [app.alpha.streams.core :refer [send-event]])
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


#_(defn send-event
    "Send kafka event. Topic is mapped by ev/type."
    {:arglists '([ev producer]
                 [ev topic producer]
                 [ev recordkey topic producer])}
    ([ev producer]
     [:ev :producer]
     (.send producer
            (event-to-topic ev)
            (event-to-recordkey ev)
            ev))
    ([ev topic producer]
     [:ev :topic :producer]
     (.send producer
            topic
            (event-to-recordkey ev)
            ev))
    ([ev k topic producer]
     [:ev :topic :producer]
     (.send producer
            topic
            k
            ev)))

#_(s/fdef send-event
    :args (s/alt :1 (s/cat :ev :ev/event
                           :producer :instance/producer)
                 :2 (s/cat :ev :ev/event
                           :topic string?
                           :producer :instance/producer)
                 :3 (s/cat :ev :ev/event
                           :topic string?
                           :k uuid?
                           :producer :instance/producer)))

(comment

  (ns-unmap *ns* 'send-event)
  (stest/instrument `send-event)
  (stest/unstrument `send-event)

  (def ev (first (gen/sample (s/gen :ev/event) 1)))

  (instance? (resolve 'org.apache.kafka.clients.producer.KafkaProducer) nil)
  (resolve 'org.apache.kafka.clients.producer.KafkaProducer)
  (send-event ev {})
  (send-event ev "asd" nil)
  (send-event ev)

  ;;
  )

(s/fdef send-event
  :args (s/cat :ev :ev/event
               :args (s/* any?)))

(comment

  (ns-unmap *ns* 'send-event)
  (stest/unstrument `send-event)
  (stest/instrument `send-event)

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "app.kafka.serdes.TransitJsonSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))
  (def ev (first (gen/sample (s/gen :ev/event) 1)))

  (isa? (class producer) :isa/kproducer)
  (send-event ev producer)
  (send-event ev "a-topic" producer)
  (send-event ev (java.util.UUID/randomUUID) producer)
  (send-event ev "a-topic" (java.util.UUID/randomUUID) producer)


  (send-event {:ev/type :ev.g.u/join1
               :u/uuid #uuid "e61b2def-ce9f-4537-8c88-cae912952534"
               :g/uuid #uuid "0de9033b-d80c-49e6-a260-ffd0d654eb2d"} producer)

  (send-event {} "a-topic" producer) ; correct - no method at :ev/event
  (send-event ev "a-topic" nil)

  (s/explain :ev/event ev)

  (type (java.util.UUID/randomUUID))
  (class (java.util.UUID/randomUUID))
  (= (type (java.util.UUID/randomUUID)) (class (java.util.UUID/randomUUID)))

  (class 1)
  (type "")
  (type {})
  (isa? nil Object)
  (ancestors (class nil))
  (ancestors (class {}))

  ;;
  )


