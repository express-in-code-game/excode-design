(ns app.repl-kafka-events
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

(defn create-user
  []
  {})


(comment
  
  (create-user)
  
  
  ;
  )
