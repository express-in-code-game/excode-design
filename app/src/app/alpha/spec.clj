(ns app.alpha.spec
  (:require [clojure.pprint :as pp]
            [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest])
  (:import java.util.Date))



(s/def :e/uuid uuid?)
(s/def :e/pos (s/tuple int? int?))
(s/def :e/numeric-value number?)
(s/def :e/type keyword?)

(s/def :e.type/teleport (s/keys :req [:e/type :e/uuid :e/pos]))
(s/def :e.type/cape (s/keys :req [:e/type :e/uuid  :e/pos]))
(s/def :e.type/value-tile (s/keys :req [:e/type :e/uuid :e/pos :e/numeric-value]))

(s/def :p/uuid uuid?)
(s/def :p/cape :e.type/cape)
(s/def :p/entites (s/keys :req [:p/cape]))
(s/def :p/sum number?)

(s/def :g/player (s/keys :req [:p/uuid :p/entites :p/sum]))

(s/def :g/uuid uuid?)
(s/def :g/status #{:created :opened :started :finished})
(s/def :g/start-inst inst?)
(s/def :g/duration-ms number?)
(s/def :g/map-size (s/tuple int? int?))
(s/def :g/player1 :g/player)
(s/def :g/player2 :g/player)
(s/def :g/exit-teleports (s/coll-of :e.type/teleport))
(s/def :g/value-tiles (s/coll-of :e.type/value-tile))
(s/def :g/observer-uuids (s/coll-of uuid?))

(s/def :g/state (s/keys :req [:g/uuid :g/status :g/start-inst
                              :g/duration-ms :g/map-size
                              :g/player1 :g/player2
                              :g/exit-teleports :g/observer-uuids
                              :g/value-tiles]))

(comment
  ; https://stackoverflow.com/questions/36639154/convert-java-util-date-to-what-java-time-type
  (def d (java.util.Date.))
  (.getTime d)
  (inst? (java.time.Instant/now))
  (inst? (java.time.Instant/now))
  (java.sql.Timestamp. 0)
  (java.sql.Timestamp.)

  (pr-str (java.util.Date.))
  (pr-str (java.time.Instant/now))
  (read-string (pr-str (java.time.Instant/now)))

  (s/explain :g/state {:g/uuid (java.util.UUID/randomUUID)
                       :g/status :created
                       :g/start-inst (java.util.Date.)
                       :g/duration-ms 60000
                       :g/map-size [128 128]
                       :g/player1 {:p/uuid #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
                                   :p/entites {:p/cape {:e/type :e.type/cape
                                                        :e/uuid (java.util.UUID/randomUUID)
                                                        :e/pos [0 0]}}
                                   :p/sum 0}
                       :g/player2 {:p/uuid #uuid "179c265a-7f72-4225-a785-2d048d575854"
                                   :p/entites {:p/cape {:e/type :e.type/cape
                                                        :e/uuid (java.util.UUID/randomUUID)
                                                        :e/pos [0 127]}}
                                   :p/sum 0}
                       :g/exit-teleports [{:e/type :e.type/teleport
                                           :e/uuid (java.util.UUID/randomUUID)
                                           :e/pos [127 0]}
                                          {:e/type :e.type/teleport
                                           :e/uuid (java.util.UUID/randomUUID)
                                           :e/pos [127 127]}]
                       :g/value-tiles (mapv (fn [x y]
                                              {:e/uuid (java.util.UUID/randomUUID)
                                               :e/type :e.type/value-tile
                                               :e/pos [x y]
                                               :e/numeric-value (inc (rand-int 10))}) (range 0 127) (range 0 127))
                       :g/observer-uuids [#uuid "46855899-838a-45fd-98b4-c76c08954645"
                                          #uuid "ea1162e3-fe45-4652-9fa9-4f8dc6c78f71"
                                          #uuid "4cd4b905-6859-4c22-bae7-ad5ec51dc3f8"]})



  ;;
  )


(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def :user/uuid uuid?)
(s/def :record/uuid uuid?)
(s/def :user/username string?)
(s/def :user/email (s/and string? #(re-matches email-regex %)))

(s/def :user/user (s/keys :req [:user/uuid :user/username :user/email]))

(s/def :event/create-user (s/and (s/keys :req [:event/type :user/uuid :user/email :user/username]
                                         :opt [])
                                 #(= (:event/type %) :event/create-user)))

(s/def :event/update-user (s/and (s/keys :req [:event/type]
                                         :opt [:user/email :user/username])
                                 #(= (:event/type %) :event/update-user)
                                 #(not-empty (select-keys % [:user/email :user/username]))))

(s/def :event/delete-record (s/and (s/keys :req [:event/type]
                                           :opt [:record/uuid])
                                   #(= (:event/type %) :event/delete-record)))

(s/def :event/uuid uuid?)
(s/def :event/player-uuid uuid?)
(s/def :event/player-event (s/and (s/keys :req [:event/type
                                                :event/uuid
                                                :event/player-uuid
                                                
                                                ])
                                  #(= (:event/type %) :event/player-event)))

(defmulti event-type (fn [x] (:event/type x)))
(defmethod event-type :event/create-user [x] :event/create-user)
(defmethod event-type :event/update-user [x] :event/update-user)
(defmethod event-type :event/delete-record [x] :event/delete-record)
(defmethod event-type :event/player-event [x] :event/player-event)
(defmethod event-type :event/arbiter-event [x] :event/arbiter-event)
(s/def :event/event (s/multi-spec event-type :event/type))

(comment

  
  (s/explain :event.game-event/player-uuid nil)

  (s/valid? :event/create-user
            {:event/type :event/create-user
             :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
             :user/email "user0@gmail.com"
             :user/username "user0"})

  (s/explain :event/update-user
             {:event/type :event/update-user
              :user/email "user0@gmail.com"
              :user/username "user0"})

  (s/valid? :event/event
            {:event/type :event/create-user
             :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
             :user/email "user0@gmail.com"
             :user/username "user0"})

  (s/conform :event/event
             {:event/type :event/create-user
              :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :user/email "user0@gmail.com"
              :user/username "user0"})

  (s/explain :event/event
             {:event/type :event/create-user
              :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :user/username "user0"})

  (s/conform :event/event
             {:event/type :event/create-user
              :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :user/username "user0"})

  (s/valid? :event/event
            {:event/type :event/update-user
             :user/email "user0@gmail.com"
             :user/username "user0"})

  (s/explain :event/create-user
             {:event/type :event/create-user
              :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :user/email "user0@gmail.com"
              :user/username "user0"})

  (s/explain :event/delete-record
             {:event/type :event/delete-record
              :record/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"})
  (s/explain :event/delete-record
             {:event/type :event/delete-record})

  ;;
  )

(comment

  (uuid?  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
  (type #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
  (java.util.UUID/fromString "5ada3765-0393-4d48-bad9-fac992d00e62")

  ;;
  )










