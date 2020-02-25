(ns app.alpha.core
  (:require [clojure.pprint :as pp]
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
  (-> (create-topics {:props props
                      :names ["alpha.user.data"]
                      :num-partitions 1
                      :replication-factor 1})
      (.all)
      (.whenComplete
       (reify KafkaFuture$BiConsumer
         (accept [this res err]
           (streams-users/mount))))))

(defn unmount
  []
  (streams-users/unmount))

(comment

  (mount)

  (unmount)
  
  (list-topics {:props props})

  (delete-topics {:props props :names ["alpha.user.data"]})

  ;;
  )