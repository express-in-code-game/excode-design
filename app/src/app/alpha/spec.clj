(ns app.alpha.spec
  (:require [clojure.pprint :as pp]
            [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest])
  (:import java.util.Date))



(s/def :g.e/uuid uuid?)
(s/def :g.e/pos (s/tuple int? int?))
(s/def :g.e/numeric-value number?)
(s/def :g.e/type keyword?)

(s/def :g.e.type/teleport (s/keys :req [:g.e/type :g.e/uuid :g.e/pos]))
(s/def :g.e.type/cape (s/keys :req [:g.e/type :g.e/uuid  :g.e/pos]))
(s/def :g.e.type/value-tile (s/keys :req [:g.e/type :g.e/uuid :g.e/pos :g.e/numeric-value]))

(s/def :p/uuid uuid?)
(s/def :p/cape :g.e.type/cape)
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
(s/def :g/exit-teleports (s/coll-of :g.e.type/teleport))
(s/def :g/value-tiles (s/coll-of :g.e.type/value-tile))
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
                                   :p/entites {:p/cape {:g.e/type :g.e.type/cape
                                                        :g.e/uuid (java.util.UUID/randomUUID)
                                                        :g.e/pos [0 0]}}
                                   :p/sum 0}
                       :g/player2 {:p/uuid #uuid "179c265a-7f72-4225-a785-2d048d575854"
                                   :p/entites {:p/cape {:g.e/type :g.e.type/cape
                                                        :g.e/uuid (java.util.UUID/randomUUID)
                                                        :g.e/pos [0 127]}}
                                   :p/sum 0}
                       :g/exit-teleports [{:g.e/type :g.e.type/teleport
                                           :g.e/uuid (java.util.UUID/randomUUID)
                                           :g.e/pos [127 0]}
                                          {:g.e/type :g.e.type/teleport
                                           :g.e/uuid (java.util.UUID/randomUUID)
                                           :g.e/pos [127 127]}]
                       :g/value-tiles (mapv (fn [x y]
                                              {:g.e/uuid (java.util.UUID/randomUUID)
                                               :g.e/type :g.e.type/value-tile
                                               :g.e/pos [x y]
                                               :g.e/numeric-value (inc (rand-int 10))}) (range 0 127) (range 0 127))
                       :g/observer-uuids [#uuid "46855899-838a-45fd-98b4-c76c08954645"
                                          #uuid "ea1162e3-fe45-4652-9fa9-4f8dc6c78f71"
                                          #uuid "4cd4b905-6859-4c22-bae7-ad5ec51dc3f8"]})



  ;;
  )


(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def :u/uuid uuid?)
(s/def :record/uuid uuid?)
(s/def :u/username string?)
(s/def :u/email (s/and string? #(re-matches email-regex %)))

(s/def :u/user (s/keys :req [:u/uuid :u/username :u/email]))

(s/def :ev.u/create (s/and (s/keys :req [:ev/type :u/uuid :u/email :u/username]
                                      :opt [])))

(s/def :ev.u/update (s/and (s/keys :req [:ev/type]
                                      :opt [:u/email :u/username])
                              #(not-empty (select-keys % [:u/email :u/username]))))

(s/def :ev.c/delete-record (s/and (s/keys :req [:ev/type]
                                          :opt [:record/uuid])))

(s/def :ev.p/move-cape (s/and (s/keys :req [:ev/type :p/uuid :g/uuid
                                            :p/cape])))

(s/def :ev.p/collect-tile-value (s/and (s/keys :req [:ev/type])))

(s/def :ev.a/finish-game (s/and (s/keys :req [:ev/type])))

(defmulti ev-type (fn [x] (:ev/type x)))
(defmethod ev-type :ev.u/create [x] :ev.u/create)
(defmethod ev-type :ev.u/update [x] :ev.u/update)
(defmethod ev-type :ev.c/delete-record [x] :ev.c/delete-record)
(defmethod ev-type :ev.p/move-cape [x] :ev.p/move-cape)
(defmethod ev-type :ev.p/collect-tile-value [x] :ev.p/collect-tile-value)
(defmethod ev-type :ev.a/finish-game [x] :ev.a/finish-game)
(s/def :ev/event (s/multi-spec ev-type :ev/type))

(defmulti ev-type-player (fn [x] (:ev/type x)) )
(defmethod ev-type-player :ev.p/move-cape [x] :ev.p/move-cape)
(defmethod ev-type-player :ev.p/collect-tile-value [x] :ev.p/collect-tile-value)
(s/def :ev.p/event (s/multi-spec ev-type-player :ev/type))

(defmulti ev-type-arbiter (fn [x] (:ev/type x)))
(defmethod ev-type-arbiter :ev.a/finish-game [x] :ev.a/finish-game)
(s/def :ev.a/event (s/multi-spec ev-type-arbiter :ev/type))

(comment

  (s/explain :event.game-event/player-uuid nil)
  (s/def :test/a-keyword keyword?)
  (s/def :test/a-string string?)
  (s/def :test/or (s/or :a :test/a-string :b :test/a-keyword))
  (s/explain :test/or ":as")

  (s/valid? :ev.u/create
            {:ev/type :ev.u/create
             :u/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
             :u/email "user0@gmail.com"
             :u/username "user0"})

  (s/explain :ev.u/update
             {:ev/type :ev.u/update
              :u/email "user0@gmail.com"
              :u/username "user0"})

  (s/conform :ev/event
             {:ev/type :ev.u/create
              :u/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :u/email "user0@gmail.com"
              :u/username "user0"})

  (s/explain :ev.p/move-cape
             {:ev/type :ev.p/move-cape
              :p/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :g/uuid (java.util.UUID/randomUUID)
              :p/cape {:g.e/uuid (java.util.UUID/randomUUID)
                       :g.e/type :g.e.type/cape
                       :g.e/pos [1 1]}})

  (s/explain :ev.p/event
             {:ev/type :ev.p/move-cape
              :p/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :g/uuid (java.util.UUID/randomUUID)
              :p/cape {:g.e/uuid (java.util.UUID/randomUUID)
                       :g.e/type :g.e.type/cape
                       :g.e/pos [1 1]}})

  (s/explain :ev.c/delete-record
             {:ev/type :ev.c/delete-record
              :record/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"})
  (s/explain :ev.c/delete-record
             {:ev/type :ev.c/delete-record})

  ;;
  )

(comment

  (uuid?  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
  (type #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
  (java.util.UUID/fromString "5ada3765-0393-4d48-bad9-fac992d00e62")

  ;;
  )










