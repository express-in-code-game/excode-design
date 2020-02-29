(ns app.alpha.repl
  (:require [clojure.pprint :as pp]
            [app.alpha.spec :as spec]

            [app.alpha.core :refer [create-topics list-topics
                                    delete-topics produce-event
                                    delete-record future-call-consumer]]
            [app.alpha.part :as part]
            [app.alpha.streams.users :as streams-users]
            [app.alpha.streams.games :as streams-games]
            [app.alpha.streams.broadcast :as streams-broadcast])
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

(def props {"bootstrap.servers" "broker1:9092"})

(def topics ["alpha.user"
             "alpha.user.changes"
             "alpha.game"
             "alpha.game.changes"])

(defn mount
  []
  (-> (create-topics {:props props
                      :names topics
                      :num-partitions 1
                      :replication-factor 1})
      (.all)
      (.whenComplete
       (reify KafkaFuture$BiConsumer
         (accept [this res err]
           (println "; created topics")
           (streams-users/mount)
           (streams-games/mount)
           (streams-broadcast/mount))))))

(defn unmount
  []
  (streams-users/unmount)
  (streams-games/unmount)
  (streams-broadcast/unmount))

(comment

  (java.util.UUID/randomUUID)

  (mount)

  (unmount)

  (list-topics {:props props})

  (delete-topics {:props props :names topics})

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "app.kafka.serdes.TransitJsonSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))
  
  (instance? org.apache.kafka.clients.producer.KafkaProducer producer)

  (def games {:a #uuid "15108e92-959d-4089-98fe-b92bb7c571db"
              :b #uuid "461b65a8-0f24-46c9-8248-4bf6d7e1aa1a"})

  (def users {:a #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :b #uuid "179c265a-7f72-4225-a785-2d048d575854"})

  (def observers {:a #uuid "46855899-838a-45fd-98b4-c76c08954645"
                  :b #uuid "ea1162e3-fe45-4652-9fa9-4f8dc6c78f71"
                  :c #uuid "4cd4b905-6859-4c22-bae7-ad5ec51dc3f8"})

  (produce-event
   "alpha.games"
   (:a games)
   {:ev/type :ev.g.u/create
    :u/uuid (:a users)})

  (produce-event
   "alpha.games"
   (:a games)
   {:ev/type :ev.g.u/delete
    :u/uuid (:a users)})

  (def fu-consumer-user-changes
    (future-call-consumer {:topic "alpha.user.changes"
                           :recordf (fn [rec]
                                      (println ";")
                                      (println (.key rec))
                                      (println (.value rec)))}))
  (future-cancel fu-consumer-user-changes)




  ;;
  )