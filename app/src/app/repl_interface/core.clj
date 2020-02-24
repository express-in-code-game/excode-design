(ns app.repl-interface.core
  (:require [clojure.pprint :as pp])
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

(defn create-topics
  [{:keys [conf
           names
           num-partitions
           replication-factor] :as opts}]
  (let [client (KafkaAdminClient/create conf)
        topics (java.util.ArrayList.
                (mapv (fn [name]
                        (NewTopic. name num-partitions (short replication-factor))) names))]
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

(defn add-shutdown-hook
  [props streams latch]
  (-> (Runtime/getRuntime)
      (.addShutdownHook (proxy
                         [Thread]
                         ["streams-shutdown-hook"]
                          (run []
                               (when (.isRunning (.state streams))
                                 (.println (System/out))
                                 (println "; closing" (.get props "application.id"))
                                 (.close streams))
                               (.countDown latch))))))

(def base-conf {"bootstrap.servers" "broker1:9092"})

(comment

  ; users creates a game -> game.data
  ;   browser tab opens
  ;   user changes settings of the game -> game.data
  ;   once finished, user presses 'invite' or 'game ready' or 'open' -> game.data game becomes visible in the list and joinable
  ;   opponent joins ( if rating >= specified by the host in settings) -> game.data
  ;   both press 'ready' -> game.data
  ;   host presses 'start the game' -> game.data
  ;   all ingame events are sent through ingame.events topic
  ;   if user closes the tab, they can reopen it from 'ongoing games' list -> get current state snapshots from game.data and ingame.events
  ;   game.data and ingame.events may have a lifespan of a day, or later possibly palyers can store up to x unfinshed games

  ; user account data only exists in user.data
  ; if user deletes their account, it gets removed from user.data
  ; in the system (event brackets, stats etc.) it get's shown as 'unknown' (only uuid is used in other topics)
  ; only events history, event placements, user wins/losses are persisted, not all games

  ; user can create lists
  ;   of other users
  ;   of events

  ; build system to 0.1 
  ;   user identity as email into uuid
  ; add security (token https wss)
  ; deploy
  ; iterate

  (def topic-names ["user.data"
                    "user.loggedin"
                    "user.connected"
                    "user.lists"
                    "event.data"
                    "event.signedup"
                    "game.data"
                    "ingame.events"])

  (create-topics {:conf base-conf
                  :names topic-names
                  :num-partitions 1
                  :replication-factor 1})

  (list-topics {:conf base-conf})

  (delete-topics {:conf base-conf
                  :names topic-names})

  (do
    (def builder (StreamsBuilder.))

    (def ktable (-> builder
                    (.stream "user.data")
                    (.groupByKey)
                    #_(.groupBy (reify KeyValueMapper
                                  (apply [this k v]
                                    (KeyValue/pair k v))))
                    (.reduce (reify Reducer
                               (apply [this ag v]
                                 (cond
                                   (= v {:delete true}) nil
                                   :else (merge ag v))))
                             (-> (Materialized/as "user.data.streams5.store")
                                 (.withKeySerde (Serdes/String))
                                 (.withValueSerde (TransitJsonSerde.))))))

    (def topology (.build builder))

    (println (.describe topology))

    (def streams-props (doto (Properties.)
                         (.putAll {"application.id" "user.data.streams5"
                                   "bootstrap.servers" "broker1:9092"
                                   "auto.offset.reset" "earliest"
                                   "default.key.serde" (.. Serdes String getClass)
                                   "default.value.serde" "app.kafka.serdes.TransitJsonSerde"})))


    (def streams (KafkaStreams. topology streams-props))

    (def latch (CountDownLatch. 1))

    (add-shutdown-hook streams-props streams latch)
    ;
    )

  (.start streams)
  (.close streams)
  (.isRunning (.state streams))

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))

  (def users {0 (.toString #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
              1 (.toString #uuid "179c265a-7f72-4225-a785-2d048d575854")
              2 (.toString #uuid "3a3e2d06-3719-4811-afec-0dffdec35543")})

  (.send producer (ProducerRecord.
                   "user.data"
                   (get users 0)
                   {:email "user0@gmail.com"
                    :username "user0"}))

  (.send producer (ProducerRecord.
                   "user.data"
                   (get users 1)
                   {:email "user1@gmail.com"
                    :username "user1"}))

  (.send producer (ProducerRecord.
                   "user.data"
                   (get users 2)
                   {:email "user2@gmail.com"
                    :username "user2"}))

  (.send producer (ProducerRecord.
                   "user.data"
                   (get users 2)
                   nil))

  (def readonly-store (.store streams "user.data.streams5.store" (QueryableStoreTypes/keyValueStore)))

  (.approximateNumEntries readonly-store)
  (doseq [x (iterator-seq (.all readonly-store))]
    (println (.key x) (.value x)))

  (.get readonly-store (get users 0))
  (.get readonly-store (get users 1))
  (.get readonly-store (get users 2))

  (.cleanUp streams)



  ;;
  )