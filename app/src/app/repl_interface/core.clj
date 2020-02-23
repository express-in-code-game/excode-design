(ns app.repl-interface.core
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
                        [(NewTopic. name num-partitions (short replication-factor))]) names))]
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
  [streams latch]
  (-> (Runtime/getRuntime)
      (.addShutdownHook (proxy
                         [Thread]
                         ["streams-shutdown-hook"]
                          (run []
                            (.println (System/out) "--closing stream")
                            (.close streams)
                            (.countDown latch))))))

(def base-conf {"bootstrap.servers" "broker1:9092"})

(comment

  (def topic-names ["user.data"
                    "user.loggedin"
                    "user.connected"
                    "event.data"
                    "event.signedup"
                    "game.data"
                    "ingame.events"
                    ])

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
  
  (create-topics {:conf base-conf
                  :names topic-names
                  :num-partitions 1
                  :replication-factor 1})

  (list-topics {:conf base-conf})

  (delete-topics {:conf base-conf
                  :names topic-names})


  
  
  ;
  )