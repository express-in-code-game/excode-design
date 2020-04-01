(ns starnet.kafka-samples.streams-example
  (:require
   [clojure.pprint :as pp])
  (:import
   org.apache.kafka.common.serialization.Serdes
   org.apache.kafka.streams.KafkaStreams
   org.apache.kafka.streams.StreamsBuilder
   org.apache.kafka.streams.StreamsConfig
   org.apache.kafka.streams.Topology
   org.apache.kafka.streams.kstream.KStream
   org.apache.kafka.streams.kstream.KTable
   java.util.Properties
   java.util.concurrent.CountDownLatch
   org.apache.kafka.clients.admin.AdminClient
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

; https://kafka.apache.org/24/documentation/streams/tutorial

(comment

  ; Streams application: Pipe
  ; https://kafka.apache.org/24/documentation/streams/tutorial#tutorial_code_pipe

  (do
    (def props (Properties.))

    (.put props "bootstrap.servers" "broker1:9092")
    (.put props "group.id" "test")
    (.put props "enable.auto.commit" "false")
    (.put props "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")
    (.put props "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")

    (def client (AdminClient/create props))
    
    ; async, cannot be executed within do block
    #_(.deleteTopics client (java.util.ArrayList. ["streams-pipe-input"
                                                   "streams-pipe-output"]))
    (def topics (java.util.ArrayList.
                 [(NewTopic. "streams-pipe-input" 1 (short 1))
                  (NewTopic. "streams-pipe-output" 1 (short 1))]))
    (.createTopics client topics)
    )

  (do
    (def props (Properties.))

    (.put props StreamsConfig/APPLICATION_ID_CONFIG "streams-pipe")
    (.put props StreamsConfig/BOOTSTRAP_SERVERS_CONFIG "broker1:9092")
    (.put props StreamsConfig/DEFAULT_KEY_SERDE_CLASS_CONFIG (.. Serdes String getClass))
    (.put props StreamsConfig/DEFAULT_VALUE_SERDE_CLASS_CONFIG (.. Serdes String getClass))

    (def builder (StreamsBuilder.))
    (def ^KStream source (.stream builder "streams-pipe-input"))
    (do (.to source "streams-pipe-output"))
    (def topology (.build builder))

    (println (.describe topology))

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
  #_(System/exit 0) ; works, streams-shutdown-hook is triggered

  (def fu-streams
    (future-call
     (fn []
       (try
         (do
           (.start streams)
           #_(.await latch)) ; .await latch halts
         (catch Exception e (.println System/out (str "caught e: " (.getMessage e))))))))

  (future-cancel fu-streams)
  (.close streams)

  (def fu-consumer
    (future-call (fn []
                   (do
                     (def consumer (KafkaConsumer.
                                    {"bootstrap.servers" "broker1:9092"
                                     "auto.offset.reset" "earliest"
                                     "auto.commit.enable" "false"
                                     "group.id" (.toString (java.util.UUID/randomUUID))
                                     "consumer.timeout.ms" "5000"
                                     "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"
                                     "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"}))

                     (.subscribe consumer (Arrays/asList (object-array ["streams-pipe-output"]))))

                   (while true
                     (let [records (.poll consumer 1000)]
                       (.println System/out (str "polling records:" (java.time.LocalTime/now)))
                       (doseq [rec records]
                         (prn (str (.key rec) " : " (.value rec)))
                         ))))))

  (future-cancel fu-consumer)

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "value.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                  "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"}))

  (.send producer (ProducerRecord. 
                   "streams-pipe-input" 
                   (.toString (java.util.UUID/randomUUID)) "all streams lead to kafka"))
  
  ;
  )


(comment

  ; Streams application: Line Split
  ; https://kafka.apache.org/24/documentation/streams/tutorial#tutorial_code_linesplit

  (do
    (def props (Properties.))
    (.put props "bootstrap.servers" "broker1:9092")
    (.put props "group.id" "test")
    (.put props "enable.auto.commit" "false")
    (.put props "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")
    (.put props "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")
    (def client (AdminClient/create props)))
  
  (do
    ; async, cannot be executed within do block
    #_(.deleteTopics client (java.util.ArrayList. ["streams-linesplit-input"
                                                   "streams-linesplit-output"]))
    (def topics (java.util.ArrayList.
                 [(NewTopic. "streams-linesplit-input" 1 (short 1))
                  (NewTopic. "streams-linesplit-output" 1 (short 1))]))
    (.createTopics client topics))
  
  (do
    (def props (Properties.))

    (.put props StreamsConfig/APPLICATION_ID_CONFIG "streams-linesplit")
    (.put props StreamsConfig/BOOTSTRAP_SERVERS_CONFIG "broker1:9092")
    (.put props StreamsConfig/DEFAULT_KEY_SERDE_CLASS_CONFIG (.. Serdes String getClass))
    (.put props StreamsConfig/DEFAULT_VALUE_SERDE_CLASS_CONFIG (.. Serdes String getClass))

    (def builder (StreamsBuilder.))
    (def ^KStream source (.stream builder "streams-linesplit-input"))
    (-> source
        (.flatMapValues
         (reify ValueMapper
           (apply [this v]
             (Arrays/asList (.split v "\\W+")))))
        (.to "streams-linesplit-output"))

    (def topology (.build builder))

    (println (.describe topology))

    (def streams (KafkaStreams. topology props))

    (def latch (CountDownLatch. 1))

    (-> (Runtime/getRuntime)
        (.addShutdownHook (proxy
                           [Thread]
                           ["streams-shutdown-hook"]
                            (run []
                              (.println (System/out) "--closing stream")
                              (.close streams)
                              (.countDown latch))))))
  #_(System/exit 0) ; works, streams-shutdown-hook is triggered

  (def fu-streams
    (future-call
     (fn []
       (try
         (do
           (.start streams)
           #_(.await latch)) ; .await latch halts
         (catch Exception e (.println System/out (str "caught e: " (.getMessage e))))))))

  (future-cancel fu-streams)
  (.close streams)

  (def fu-consumer
    (future-call (fn []
                   (do
                     (def consumer (KafkaConsumer.
                                    {"bootstrap.servers" "broker1:9092"
                                     "auto.offset.reset" "earliest"
                                     "auto.commit.enable" "false"
                                     "group.id" (.toString (java.util.UUID/randomUUID))
                                     "consumer.timeout.ms" "5000"
                                     "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"
                                     "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"}))

                     (.subscribe consumer (Arrays/asList (object-array ["streams-linesplit-output"]))))

                   (while true
                     (let [records (.poll consumer 1000)]
                       (.println System/out (str "polling records:" (java.time.LocalTime/now)))
                       (doseq [rec records]
                         (prn (str (.key rec) " : " (.value rec)))))))))

  (future-cancel fu-consumer)

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "value.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                  "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"}))

  (.send producer (ProducerRecord.
                   "streams-linesplit-input"
                   (.toString (java.util.UUID/randomUUID)) "all streams lead to kafka"))

  ;
  )


(comment

  ; Streams application: statefull wordcount
  ; https://kafka.apache.org/24/documentation/streams/tutorial#tutorial_code_wordcount

  (do
    (def props (Properties.))

    (.put props "bootstrap.servers" "broker1:9092")
    (.put props "group.id" "test")
    (.put props "log.cleanup.policy" "compact")
    (.put props "cleanup.policy" "compact")
    (.put props "enable.auto.commit" "false")
    (.put props "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")
    (.put props "value.deserializer" "org.apache.kafka.common.serialization.StringDeserializer")

    (def client (AdminClient/create props)))

  (do
    ; async, cannot be executed within do block
    #_(.deleteTopics client (java.util.ArrayList. ["streams-wordcount-stateful-input"
                                                   "streams-wordcount-stateful-output"]))
    (def topics (java.util.ArrayList.
                 [(NewTopic. "streams-wordcount-stateful-input" 1 (short 1))
                  (NewTopic. "streams-wordcount-stateful-output" 1 (short 1))]))
    (.createTopics client topics))


  (do
    (def props (Properties.))

    (.put props StreamsConfig/APPLICATION_ID_CONFIG "streams-wordcount")
    (.put props StreamsConfig/BOOTSTRAP_SERVERS_CONFIG "broker1:9092")
    (.put props StreamsConfig/DEFAULT_KEY_SERDE_CLASS_CONFIG (.. Serdes String getClass))
    (.put props StreamsConfig/DEFAULT_VALUE_SERDE_CLASS_CONFIG (.. Serdes String getClass))

    (def builder (StreamsBuilder.))
    (def ^KStream source (.stream builder "streams-wordcount-stateful-input"))
    (-> source
        (.flatMapValues
         (reify ValueMapper
           (apply [this v]
             (Arrays/asList (-> v (.toLowerCase (Locale/getDefault)) (.split "\\W+"))))))
        (.groupBy
         (reify KeyValueMapper
           (apply [this k v]
             v)))
        (.count (Materialized/as "counts-store"))
        (.toStream)
        (.to "streams-wordcount-stateful-output" (Produced/with (Serdes/String) (Serdes/Long))))

    (def topology (.build builder))

    (println (.describe topology))

    (def streams (KafkaStreams. topology props))

    (def latch (CountDownLatch. 1))

    (-> (Runtime/getRuntime)
        (.addShutdownHook (proxy
                           [Thread]
                           ["streams-shutdown-hook"]
                            (run []
                              (.println (System/out) "--closing stream")
                              (.close streams)
                              (.countDown latch))))))

  ; https://kafka.apache.org/24/documentation/streams/developer-guide/write-streams
  (.setUncaughtExceptionHandler streams
                                (reify Thread$UncaughtExceptionHandler
                                  (uncaughtException [this thred thrwable]
                                    (prn "This handler is called whenever a stream thread is terminated by an unexpected exception"))))

  (def fu-streams
    (future-call
     (fn []
       (try
         (do
           (.start streams)
           #_(.await latch)) ; .await latch halts
         (catch Exception e (.println System/out (str "caught e: " (.getMessage e))))))))

  (future-cancel fu-streams)
  (.close streams)

  (def fu-consumer
    (future-call (fn []
                   (do
                     (def consumer (KafkaConsumer.
                                    {"bootstrap.servers" "broker1:9092"
                                     "auto.offset.reset" "earliest"
                                     "auto.commit.enable" "false"
                                     "group.id" (.toString (java.util.UUID/randomUUID))
                                     "consumer.timeout.ms" "5000"
                                     "key.deserializer" "org.apache.kafka.common.serialization.StringDeserializer"
                                     "value.deserializer" "org.apache.kafka.common.serialization.LongDeserializer"}))

                     (.subscribe consumer (Arrays/asList (object-array ["streams-wordcount-stateful-output"]))))

                   (while true
                     (let [records (.poll consumer 1000)]
                       (.println System/out (str "polling records:" (java.time.LocalTime/now)))
                       (doseq [rec records]
                         (prn (str (.key rec) " : " (.value rec)))))))))

  (future-cancel fu-consumer)

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "value.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                  "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"}))

  ; why takes too long to process ..?

  (.send producer (ProducerRecord.
                   "streams-wordcount-stateful-input"
                   (.toString (java.util.UUID/randomUUID)) "all streams lead to kafka"))


  ;
  )