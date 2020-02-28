(ns app.alpha.core
  (:require [clojure.pprint :as pp]
            [app.alpha.spec :as spec]
            [app.alpha.spec-test :as spec-test]
            [app.alpha.streams.users :as streams-users]
            [app.alpha.streams.core :refer [create-topics list-topics
                                            delete-topics]]
            [app.alpha.part :as part]
            [app.alpha.streams.users])
  (:import
   org.apache.kafka.common.KafkaFuture$BiConsumer))

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

(defn env-optimized?
  []
  (let [appenv (read-string (System/getenv "appenv"))]
    (:optimized appenv)))

(defn mount
  []
  (when-not (env-optimized?)
    (spec-test/instrument))
  #_(-> (create-topics {:props props
                        :names ["alpha.user.data"
                                "alpha.user.data.changes"]
                        :num-partitions 1
                        :replication-factor 1})
        (.all)
        (.whenComplete
         (reify KafkaFuture$BiConsumer
           (accept [this res err]
             (streams-users/mount))))))

(defn unmount
  []
  (spec-test/unstrument)
  #_(streams-users/unmount))

(comment

  (mount)

  (unmount)

  (list-topics {:props props})

  (delete-topics {:props props :names ["alpha.user.data"
                                       "alpha.user.data.changes"]})
  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))

  (def users {0 (.toString #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
              1 (.toString #uuid "179c265a-7f72-4225-a785-2d048d575854")
              2 (.toString #uuid "3a3e2d06-3719-4811-afec-0dffdec35543")})

  (.send producer (ProducerRecord.
                   "game.events"
                   (get users 0)
                   :game-event-here))


  ;;
  )