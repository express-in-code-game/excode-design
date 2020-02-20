(ns app.kafka.wordcount
  (:require [clojure.pprint :as pp])
  (:import
   org.apache.kafka.clients.consumer.ConsumerConfig
   org.apache.kafka.common.serialization.Serdes
   org.apache.kafka.streams.KafkaStreams
   org.apache.kafka.streams.StreamsBuilder
   org.apache.kafka.streams.StreamsConfig
   org.apache.kafka.streams.kstream.KStream
   org.apache.kafka.streams.kstream.KTable
   org.apache.kafka.streams.kstream.Produced
   org.apache.kafka.streams.kstream.ValueMapper
   org.apache.kafka.streams.kstream.KeyValueMapper

   org.apache.kafka.clients.admin.AdminClient
   org.apache.kafka.clients.admin.NewTopic
   org.apache.kafka.clients.consumer.KafkaConsumer
   org.apache.kafka.clients.producer.KafkaProducer
   org.apache.kafka.common.serialization.Serde
   org.apache.kafka.clients.producer.ProducerRecord

   java.util.Arrays
   java.util.Locale
   java.util.Properties
   java.util.concurrent.CountDownLatch))

; https://kafka.apache.org/24/documentation/streams/quickstart

; https://github.com/apache/kafka/blob/2.4/streams/examples/src/main/java/org/apache/kafka/streams/examples/wordcount/WordCountDemo.java
; https://github.com/apache/kafka/blob/2.4.0/streams/src/main/java/org/apache/kafka/streams/kstream/internals/KStreamImpl.java
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
               [(NewTopic. "streams-plaintext-input" 1 (short 1))
                (NewTopic. "streams-wordcount-output" 1 (short 1))]))

  (.createTopics client topics)
  #_(.deleteTopics client (java.util.ArrayList. ["streams-plaintext-input"
                                                 "streams-wordcount-output"]))

  (def consumer (KafkaConsumer. props))
  (.listTopics consumer)

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "kafka1:9092"
                  "value.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                  "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"}))

  (.send producer (ProducerRecord. "streams-plaintext-input" "key1" "all streams lead to kafka"))

  (do

    (def stream-props (Properties.))

    (.put stream-props StreamsConfig/APPLICATION_ID_CONFIG "streams-wordcount")
    (.put stream-props StreamsConfig/BOOTSTRAP_SERVERS_CONFIG "kafka1:9092")
    (.put stream-props StreamsConfig/CACHE_MAX_BYTES_BUFFERING_CONFIG 0)
    (.put stream-props StreamsConfig/DEFAULT_KEY_SERDE_CLASS_CONFIG (.. Serdes (String) (getClass) (getName)))
    (.put stream-props StreamsConfig/DEFAULT_VALUE_SERDE_CLASS_CONFIG (.. Serdes (String) (getClass) (getName)))
    (.put stream-props ConsumerConfig/AUTO_OFFSET_RESET_CONFIG "earliest")

    (def streams-builder (StreamsBuilder.))
    (def source (.stream streams-builder "streams-plaintext-input"))
    (def counts
      (-> source
        ; https://kafka.apache.org/24/javadoc/org/apache/kafka/streams/kstream/KStream.html
          (.flatMapValues
         ; https://github.com/troy-west/kstream-examples/blob/master/test/troy_west/kstream/examples.clj#L47
         ; https://kafka.apache.org/24/javadoc/org/apache/kafka/streams/kstream/ValueMapper.html
           (reify ValueMapper
           ; https://stackoverflow.com/questions/34902518/errors-extending-a-java-interface-in-clojure
             (apply [this vl]
               (.println System/out ".flatMapValues apply call:")
               (.println System/out vl)
               (.println System/out (-> vl (.toLowerCase (Locale/getDefault)) (.split " ")))
               (Arrays/asList (-> vl (.toLowerCase (Locale/getDefault)) (.split " "))))))
          (.groupBy
           (reify KeyValueMapper
             (apply [this k vl]
               (.println System/out ".groupBy apply call:")
               (.println System/out vl)
               vl)))
          (.count)))

    (-> counts
        (.toStream)
        (.to "streams-wordcount-output" (Produced/with (Serdes/String) (Serdes/Long)))))

  (def streams (KafkaStreams. (.build streams-builder) stream-props))

  (def latch (CountDownLatch. 1))

  ; https://github.com/perkss/clojure-kafka-examples/blob/4902b07b965c096f93874fb035c10259c4c48dfb/kafka-streams-example/src/kafka_streams_example/core.clj#L23
  (do
    (.addShutdownHook (Runtime/getRuntime) (Thread.
                                            (fn []
                                              (.close streams)
                                              (.countDown latch)))))

  (try
    (do
      (.start streams)
      #_(.await latch))
    (catch Exception e (prn (str "caught exception: " (.getMessage e)))))




  (def fu (future-call (fn []
                         (do
                           (def consumer (KafkaConsumer.
                                          {"bootstrap.servers" "kafka1:9092"
                                           "auto.offset.reset" "earliest"
                                           "auto.commit.enable" "false"
                                           "group.id" (.toString (java.util.UUID/randomUUID))
                                           "consumer.timeout.ms" "5000"
                                           "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"
                                           "value.deserializer" "org.apache.kafka.common.serialization.LongDeserializer"}))

                           (.subscribe consumer (Arrays/asList (object-array ["streams-wordcount-output"]))))

                         (def x (atom nil))
                         (while true
                           (let [records (.poll consumer 1000)]
                             (.println System/out "polling records:")
                             (doseq [rec records]
                               (reset! x rec)
                               (prn (str (.key rec) " " (.value rec)  ))
                               #_(.println System/out (str "value: " (.value rec)))))))))
  
  (future-cancel fu)

  (.send producer (ProducerRecord. "streams-plaintext-input" "key1" "hello kafka streams"))
  (.send producer (ProducerRecord. "streams-plaintext-input" "key1" "join kafka summit"))

  (.send producer (ProducerRecord. "streams-plaintext-input" "key1" "sequences"))

  ;
  )
