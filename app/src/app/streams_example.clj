(ns app.streams-example
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
   java.util.ArrayList
   ))

(comment

  (def props (Properties.))
  (do
    (.setProperty props "bootstrap.servers" "kafka1:9092")
    (.setProperty props "group.id" "test")
    (.setProperty props "enable.auto.commit" "true")
    (.setProperty props "auto.commit.interval.ms" "1000")
    (.setProperty props "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")
    (.setProperty props "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"))

  (def client (AdminClient/create props))

  (def topics (java.util.ArrayList.
               [(NewTopic. "topic1" 1 (short 1))
                (NewTopic. "topic2" 1 (short 1))]))

  (.createTopics client topics)

  (.listTopics client)

  (def consumer (KafkaConsumer. props))
  (count (.listTopics consumer))
  (butlast (.listTopics consumer))

  (.deleteTopics client (java.util.ArrayList. ["topic1"
                                               "topic2"
                                               "streams-plaintext-input1"]))
  
  
  ;
  )

(comment
  
  ; word count

  (def props (Properties.))
  (do
    (.setProperty props "bootstrap.servers" "kafka1:9092")
    (.setProperty props "group.id" "test")
    (.setProperty props "enable.auto.commit" "true")
    (.setProperty props "auto.commit.interval.ms" "1000")
    (.setProperty props "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")
    (.setProperty props "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")
    )

  (def client (AdminClient/create props))

  (def topic1 
    (NewTopic. "streams-plaintext-input" 1 (short 1))
    )
  
  (def topic2
    (NewTopic. "streams-wordcount-output" 1 (short 1)))
  
  (.configs topic2 {"cleanup.policy" "compact"})
  
  (.createTopics client (java.util.ArrayList.
                         [topic1
                          topic2]))
  
  (def consumer (KafkaConsumer. props))
  
  (butlast (.listTopics consumer))
  
  
  

  ;
  )


(comment

  (do
    (def props (Properties.))

    (.put props StreamsConfig/APPLICATION_ID_CONFIG "streams-pipe")
    (.put props StreamsConfig/BOOTSTRAP_SERVERS_CONFIG "kafka1:9092")
    (.put props StreamsConfig/DEFAULT_KEY_SERDE_CLASS_CONFIG (.. Serdes String getClass))
    (.put props StreamsConfig/DEFAULT_VALUE_SERDE_CLASS_CONFIG (.. Serdes String getClass))

    (def builder (StreamsBuilder.))

    (-> builder
        (.stream  "streams-plaintext-input")
        (.to "streams-pipe-output"))

    (def topology (.build builder))

    (def streams (KafkaStreams. topology props))

    (def latch (CountDownLatch. 1))

    (-> (Runtime/getRuntime)
        (.addShutdownHook (proxy
                           [Thread]
                           ["streams-shutdown-hook"]
                            (run []
                              (.println (System/out) "--closing stream")
                              (.close streams)
                              (.countDown latch)))))
    )
  
  (.println (System/out) "3")
  
  (future-call (fn []
                 (try
                   (.start streams)
                   (.await latch)
                   (catch Exception e
                     (do
                       (prn (.getMessage e))
                       #_(System/exit 1))))
                 ))
  
  (System/exit 0)
  ;
  )




(comment
  
  
  
  
  
  
  ;
  )
