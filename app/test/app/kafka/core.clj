(ns app.kafka.core
  (:require
   [clojure.pprint :as pp])
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

; simulate, experiment with events, sequences
; persist to docker volumes
; gen large seqs with clojure.spec - thousands of games, users etc.
; no ksql

; https://kafka.apache.org/
; https://kafka-tutorials.confluent.io/
; https://kafka.apache.org/24/documentation/streams/developer-guide

; https://kafka.apache.org/24/javadoc/org/apache/kafka/streams/kstream/KStream.html
; https://kafka.apache.org/24/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html
; https://kafka.apache.org/24/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html

; https://github.com/perkss/clojure-kafka-examples
; https://github.com/troy-west/kstream-examples

; https://www.confluent.io/blog/event-sourcing-using-apache-kafka/
; https://www.confluent.io/blog/building-a-microservices-ecosystem-with-kafka-streams-and-ksql/
; https://github.com/confluentinc/kafka-streams-examples/tree/4.0.0-post/src/main/java/io/confluent/examples/streams/microservices

; configs

; http://kafka.apache.org/documentation.html#configuration
; http://kafka.apache.org/documentation.html#topicconfigs

; http://kafka.apache.org/documentation.html#producerconfigs
; https://kafka.apache.org/24/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html

; http://kafka.apache.org/documentation.html#consumerconfigs
; https://kafka.apache.org/24/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html