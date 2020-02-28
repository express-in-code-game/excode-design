(ns app.alpha.spec
  (:require [clojure.pprint :as pp]
            [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest])
  (:import java.util.Date))

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

(s/def :event/delete-record (s/and (s/keys :req [:event/type ]
                                           :opt [:record/uuid])
                                   #(= (:event/type %) :event/delete-record)
                                   ))

(comment

  (s/valid? :event/create-user
            {:event/type :event/create-user
             :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
             :user/email "user0@gmail.com"
             :user/username "user0"})

  (s/explain :event/update-user
             {:event/type :event/update-user
              :user/email "user0@gmail.com"
              :user/username "user0"})

  ;;
  )


(defmulti event-type (fn [x] (:event/type x)))
(defmethod event-type :event/create-user [x] :event/create-user)
(defmethod event-type :event/update-user [x] :event/update-user)
(defmethod event-type :event/delete-record [x] :event/delete-record)
(s/def :event/event (s/multi-spec event-type :event/type))

(comment


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

(s/def :game/uuid uuid?)
(s/def :game/status #{:created :opened :started :finished})
(s/def :game/start-inst inst?)
(s/def :game/duration-ms number?)
(s/def :game/map-size (s/tuple int? int?))
(s/def :game/player1-uuid uuid?)
(s/def :game/player2-uuid uuid?)
(s/def :game/player1-cape-pos (s/tuple int? int?))
(s/def :game/player2-cape-pos (s/tuple int? int?))
(s/def :game/player1-sum number?)
(s/def :game/player2-sum number?)
(s/def :game/teleport1-pos (s/tuple int? int?))
(s/def :game/teleport2-pos (s/tuple int? int?))


(s/def :game/state (s/keys :req [:game/uuid :game/status :game/start-inst
                                 :game/duration-ms :game/player1-uuid :game/player2-uuid
                                 :game/player1-cape-pos :game/player1-cape-pos :game/player1-sum
                                 :game/player2-sum :game/teleport1-pos :game/teleport2-pos :game/map-size]))

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

  (s/explain :game/state {:game/uuid (java.util.UUID/randomUUID)
                          :game/status :created
                          :game/start-inst (java.util.Date.)
                          :game/duration-ms 60000
                          :game/map-size [128 128]
                          :game/player1-uuid #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
                          :game/player2-uuid #uuid "179c265a-7f72-4225-a785-2d048d575854"
                          :game/player1-cape-pos [0 0]
                          :game/player2-cape-pos [0 127]
                          :game/player1-sum 0
                          :game/player2-sum 0
                          :game/teleport1-pos [127 0]
                          :game/teleport2-pos [127 127]})



  ;;
  )