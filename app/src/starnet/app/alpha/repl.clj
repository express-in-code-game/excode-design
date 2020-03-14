(ns starnet.app.alpha.repl
  (:require
   [clojure.pprint :as pp]
   [clojure.spec.alpha :as s]
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub]]
   [starnet.app.alpha.streams :refer [produce-event
                                      future-call-consumer read-store
                                      send-event create-streams-game create-streams-user]])
  (:import
   starnet.app.alpha.aux.serdes.TransitJsonSerializer
   starnet.app.alpha.aux.serdes.TransitJsonDeserializer
   starnet.app.alpha.aux.serdes.TransitJsonSerde
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
   org.apache.kafka.common.KafkaFuture$BiConsumer
   java.util.ArrayList
   java.util.Locale
   java.util.Arrays))

(comment

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
  
  ;;
  )

(def games {0 #uuid "15108e92-959d-4089-98fe-b92bb7c571db"
            1 #uuid "461b65a8-0f24-46c9-8248-4bf6d7e1aa1a"})

(def users {0 #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              1 #uuid "179c265a-7f72-4225-a785-2d048d575854"})

(def observers {0 #uuid "46855899-838a-45fd-98b4-c76c08954645"
                1 #uuid "ea1162e3-fe45-4652-9fa9-4f8dc6c78f71"
                2 #uuid "4cd4b905-6859-4c22-bae7-ad5ec51dc3f8"})

(comment

  (def p (KafkaProducer.
          {"bootstrap.servers" "broker1:9092"
           "auto.commit.enable" "true"
           "key.serializer" "starnet.app.alpha.aux.serdes.TransitJsonSerializer"
           "value.serializer" "starnet.app.alpha.aux.serdes.TransitJsonSerializer"}))

  (def state-user (create-streams-user))
  (def streams-user (:kstreams state-user))
  (.isRunning (.state streams-user))
  (.start streams-user)
  (.close streams-user)
  (.cleanUp streams-user)

  (def store-user (.store streams-user "alpha.user.streams.store" (QueryableStoreTypes/keyValueStore)))
  (.approximateNumEntries store-game)
  (count (read-store store-user))
  (read-store store-user)
  (read-store store-user :offset 1 :limit 1 :intomap? true)
  (read-store store-user :offset 1 :limit 1 :intomap? false :fval #(select-keys % [:u/email]))

  (send-event {:ev/type :ev.u/create
               :u/uuid  (get users 0)
               :u/email "user0@gmail.com"
               :u/name "user0"} p)
  (.get store-user (get users 0))

  (send-event {:ev/type :ev.u/create
               :u/uuid  (get users 1)
               :u/email "user1@gmail.com"
               :u/name "user1"} p)
  (.get store-user (get users 1))

  (send-event {:ev/type :ev.u/update
               :u/uuid  (get users 0)
               :u/email "user0@gmail.com"
               :u/name "user0"} p)
  (.get store-user (get users 0))

  (send-event {:ev/type :ev.u/delete
               :u/uuid  (get users 0)} p)
  (.get store-user (get users 1))


  (def fu-consumer-user-changes
    (future-call-consumer {:topic "alpha.user.changes"
                           :recordf (fn [rec]
                                      (println ";")
                                      (println (.key rec))
                                      (println (.value rec)))}))
  (future-cancel fu-consumer-user-changes)


  (def state-game (create-streams-game))
  (def streams-game (:kstreams state-game))
  (.start streams-game)
  (.isRunning (.state streams-game))
  (.close streams-game)
  (.cleanUp streams-game)
  (def store-game (.store streams-game "alpha.game.streams.store" (QueryableStoreTypes/keyValueStore)))

  (read-store store-game :fval  #(select-keys % [:g/uuid :g/status :g/start-inst :u/uuid]))
  (read-store store-game)

  (send-event {:ev/type :ev.g.u/create
               :u/uuid  (get users 0)}
              (get games 0) p)

  (send-event {:ev/type :ev.g.u/configure
               :u/uuid (get users 0)
               :g/uuid (get games 0)
               :g/status :opened}
              p)

  (send-event {:ev/type :ev.g.u/configure
               :u/uuid (get users 0)
               :g/uuid (get games 0)
               :g/status :started}
              p)



  ;;;

  (select-keys {:a 1 :b 2} [:a])
  (s/explain :instance/producer producer)
  (java.util.UUID/randomUUID)
  ;;
  )