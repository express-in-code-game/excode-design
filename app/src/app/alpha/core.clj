(ns app.alpha.core
  (:require [clojure.pprint :as pp]
            [app.alpha.spec :as spec]
            [app.alpha.spec-test :as spec-test]
            [app.alpha.streams.users :as streams-users]
            [app.alpha.streams.core :refer [create-topics list-topics
                                            delete-topics]])
  (:import
   org.apache.kafka.common.KafkaFuture$BiConsumer))

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

  ;;
  )

(def props {"bootstrap.servers" "broker1:9092"})

(defn mount
  []
  (spec-test/instrument)
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

  ; player 5min games from repl
  ; emit events for players
  ; arbiter emits on interval while depending on current game states
  ; games computes states from game.events
  ; broadcast prints to the stdout

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