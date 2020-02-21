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
; https://github.com/troy-west/kstream-examples/blob/master/test/troy_west/kstream/examples.clj

; https://www.confluent.io/blog/event-sourcing-using-apache-kafka/
; https://www.confluent.io/blog/building-a-microservices-ecosystem-with-kafka-streams-and-ksql/
; https://github.com/confluentinc/kafka-streams-examples/tree/4.0.0-post/src/main/java/io/confluent/examples/streams/microservices


(comment

 ; repl interface

  create-user
  delete-account
  change-username
  change-email
  list-users
  list-user-account
  list-user-ongoing-games
  list-user-game-history
  create-event
  :event.type/single-elemination-bracket
  :event/start-ts
  cancel-event
  signin-event
  signout-event
  list-events
  list-event-signedup-users
  create-game
  cancel-game
  start-game
  end-game
  list-games
  join-game
  invite-into-game
  connect-to-game
  disconnect-from-game
  ingame-event
  list-ingame-events-for-game

  ;
  )


