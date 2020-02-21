(ns app.kafka.repl-kafka-events
  (:require [clojure.pprint :as pp])
  (:import
   org.apache.kafka.common.serialization.Serdes
   org.apache.kafka.streams.KafkaStreams
   org.apache.kafka.streams.StreamsBuilder
   org.apache.kafka.streams.StreamsConfig
   org.apache.kafka.streams.Topology
   java.util.Properties
   java.util.concurrent.CountDownLatch
   org.apache.kafka.clients.admin.AdminClient
   org.apache.kafka.clients.admin.NewTopic
   org.apache.kafka.clients.consumer.KafkaConsumer
   java.util.ArrayList))

; simulate, experiment with events, sequences
; persist to docker
; gen large seqs with clojure.spec - thousands of games, users etc.

; https://kafka.apache.org/
; https://kafka-tutorials.confluent.io/
; https://github.com/perkss/clojure-kafka-examples
; https://github.com/troy-west/kstream-examples/blob/master/test/troy_west/kstream/examples.clj

; https://kafka.apache.org/24/javadoc/org/apache/kafka/streams/kstream/KStream.html 
; https://kafka.apache.org/24/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html

; https://kafka.apache.org/24/documentation/streams/developer-guide/config-streams

(defn create-user
  []
  {})


(comment
  
  (create-user)
  
  
  ;
  )
