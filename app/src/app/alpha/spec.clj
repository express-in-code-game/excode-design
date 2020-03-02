(ns app.alpha.spec
  (:require [clojure.pprint :as pp]
            [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest])
  (:import 
   java.util.Date
   org.apache.kafka.clients.producer.KafkaProducer
   org.apache.kafka.clients.consumer.KafkaConsumer
   ))

(derive java.util.Map :isa/map)
(derive java.util.Set :isa/set)
(derive java.util.UUID :isa/uuid)
(derive org.apache.kafka.clients.producer.KafkaProducer :isa/kproducer)
(derive org.apache.kafka.clients.consumer.KafkaConsumer :isa/kconsumer)
(derive clojure.lang.Keyword :isa/keyword)

(s/def :instance/kproducer #(instance? org.apache.kafka.clients.producer.KafkaProducer %))
(s/def :instance/kconsumer #(instance? org.apache.kafka.clients.consumer.KafkaConsumer %))


(s/def :g.e/uuid uuid?)
(s/def :g.e/pos (s/tuple int? int?))
(s/def :g.e/numeric-value number?)
(s/def :g.e/type keyword?)

(s/def :g.e.type/teleport (s/keys :req [:g.e/type :g.e/uuid :g.e/pos]))
(s/def :g.e.type/cape (s/keys :req [:g.e/type :g.e/uuid  :g.e/pos]))
(s/def :g.e.type/value-tile (s/keys :req [:g.e/type :g.e/uuid :g.e/pos :g.e/numeric-value]))

(s/def :g.p/cape :g.e.type/cape)
(s/def :g.p/entities (s/keys :req [:g.p/cape]))
(s/def :g.p/sum number?)

(s/def :g.p/player (s/keys :req [:g.p/entities :g.p/sum]))

(s/def :g.r/host (s/nilable boolean?))
(s/def :g.r/player (s/nilable int?))
(s/def :g.r/observer (s/nilable boolean?))
(s/def :g.r/role (s/keys :req [:g.r/host :g.r/player :g.r/observer]))

(s/def :g/uuid uuid?)
(s/def :g/status #{:created :opened :started :finished})
(s/def :g/start-inst inst?)
(s/def :g/duration-ms number?)
(s/def :g/map-size (s/tuple int? int?))
(s/def :g/roles (s/map-of uuid? :g.r/role))
(s/def :g/player-states (s/map-of int? :g.p/player))
(s/def :g/exit-teleports (s/coll-of :g.e.type/teleport))
(s/def :g/value-tiles (s/coll-of :g.e.type/value-tile))


(s/def :g/game (s/keys :req [:g/uuid :g/status :g/start-inst
                             :g/duration-ms :g/map-size
                             :g/roles :g/player-states
                             :g/exit-teleports
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

  (s/explain :g/game {:g/uuid (java.util.UUID/randomUUID)
                      :g/status :created
                      :g/start-inst (java.util.Date.)
                      :g/duration-ms 60000
                      :g/map-size [128 128]
                      :g/roles {#uuid "5ada3765-0393-4d48-bad9-fac992d00e62" {:g.r/host true
                                                                              :g.r/player 0
                                                                              :g.r/observer false}}
                      :g/player-states {0 {:g.p/entities {:g.p/cape {:g.e/type :g.e.type/cape
                                                                :g.e/uuid (java.util.UUID/randomUUID)
                                                                :g.e/pos [0 0]}}
                                           :g.p/sum 0}
                                        1 {:g.p/entities {:g.p/cape {:g.e/type :g.e.type/cape
                                                                     :g.e/uuid (java.util.UUID/randomUUID)
                                                                     :g.e/pos [0 15]}}
                                           :g.p/sum 0}}
                      :g/exit-teleports [{:g.e/type :g.e.type/teleport
                                          :g.e/uuid (java.util.UUID/randomUUID)
                                          :g.e/pos [15 0]}
                                         {:g.e/type :g.e.type/teleport
                                          :g.e/uuid (java.util.UUID/randomUUID)
                                          :g.e/pos [15 15]}]
                      :g/value-tiles (-> (mapcat (fn [x]
                                                   (mapv (fn [y]
                                                           {:g.e/uuid (java.util.UUID/randomUUID)
                                                            :g.e/type :g.e.type/value-tile
                                                            :g.e/pos [x y]
                                                            :g.e/numeric-value (inc (rand-int 10))}) (range 0 16)))
                                                 (range 0 16))
                                         (vec))})

  (gen/generate (s/gen :g/game))


  (gen/sample ev-p-move-cape-gen 1)

  ;;
  )


(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def :u/uuid uuid?)
(s/def :record/uuid uuid?)
(s/def :u/username string?)
(s/def :u/email (s/with-gen
                  (s/and string? #(re-matches email-regex %))
                  #(gen/fmap (fn [s]
                               (str s "@gmail.com"))
                             (gen/such-that (fn [s] (not= s ""))
                                            (gen/string-alphanumeric)))))

#_(gen/sample (s/gen :u/email))

#_(s/def :test/uuids (s/coll-of uuid?))
#_(s/explain :test/uuids [(java.util.UUID/randomUUID) (java.util.UUID/randomUUID)])

(s/def :u/user (s/keys :req [:u/uuid :u/username :u/email]))

(s/def :ev/type keyword?)

(s/def :ev.c/delete-record (s/keys :req [:ev/type]
                                   :opt [:record/uuid]))

(s/def :ev.u/create (s/keys :req [:ev/type :u/uuid :u/email :u/username]
                            :opt []))
(s/def :ev.u/update (s/keys :req [:ev/type]
                                 :opt [:u/email :u/username]))
(s/def :ev.u/delete (s/keys :req [:ev/type]
                            :opt []))
(s/def :ev.g.u/create (s/keys :req [:ev/type :u/uuid]
                                 :opt []))

(s/def :ev.g.u/delete (s/keys :req [:ev/type]
                              :opt []))

(s/def :ev.g.u/delete (s/keys :req [:ev/type :u/uuid :g/uuid]
                                 :opt []))

(s/def :ev.g.u/configure (s/keys :req [:ev/type :u/uuid :g/uuid]
                                    :opt []))

(s/def :ev.g.u/start (s/keys :req [:ev/type :u/uuid :g/uuid]
                                :opt []))

(s/def :ev.g.u/join (s/keys :req [:ev/type :u/uuid :g/uuid]
                               :opt []))

(s/def :ev.g.u/leave (s/keys :req [:ev/type :u/uuid :g/uuid]
                                :opt []))

(s/def :ev.g.p/move-cape (s/keys :req [:ev/type :u/uuid :g/uuid
                                       :g.p/cape]))
(def gen-ev-p-move-cape (gen/fmap (fn [x]
                                    (merge
                                     x
                                     {:ev/type :ev.g.p/move-cape}))
                                  (s/gen :ev.g.p/move-cape)))


(s/def :ev.g.p/collect-tile-value (s/and (s/keys :req [:ev/type])))

(s/def :ev.g.a/finish-game (s/and (s/keys :req [:ev/type])))

(def gen-ev-a-finish-game (gen/fmap (fn [x]
                                      (merge
                                       x
                                       {:ev/type :ev.g.a/finish-game}))
                                    (s/gen :ev.g.a/finish-game)))

(defmulti ev (fn [x] (:ev/type x)))
(defmethod ev :ev.u/create [x] :ev.u/create)
(defmethod ev :ev.u/update [x] :ev.u/update)
(defmethod ev :ev.u/delete [x] :ev.u/delete)
(defmethod ev :ev.g.u/create [x] :ev.g.u/create)
(defmethod ev :ev.g.u/delete [x] :ev.g.u/delete)
(defmethod ev :ev.g.u/configure [x] :ev.g.u/configure)
(defmethod ev :ev.g.u/start [x] :ev.g.u/start)
(defmethod ev :ev.g.u/join [x] :ev.g.u/join)
(defmethod ev :ev.g.u/leave [x] :ev.g.u/leave)
(defmethod ev :ev.c/delete-record [x] :ev.c/delete-record)
(defmethod ev :ev.g.p/move-cape [x] :ev.g.p/move-cape)
(defmethod ev :ev.g.p/collect-tile-value [x] :ev.g.p/collect-tile-value)
(defmethod ev :ev.g.a/finish-game [x] :ev.g.a/finish-game)
(s/def :ev/event (s/multi-spec ev :ev/type))

(defmulti ev-game-player (fn [x] (:ev/type x)) )
(defmethod ev-game-player :ev.g.p/move-cape [x] :ev.g.p/move-cape)
(defmethod ev-game-player :ev.g.p/collect-tile-value [x] :ev.g.p/collect-tile-value)
(s/def :ev.g.p/event (s/multi-spec ev-game-player :ev/type))

(defmulti ev-game-arbiter (fn [x] (:ev/type x)))
(defmethod ev-game-arbiter :ev.g.a/finish-game [x] :ev.g.a/finish-game)
(s/def :ev.g.a/event (s/multi-spec ev-game-arbiter :ev/type))

(defmulti ev-game-member (fn [x] (:ev/type x)))
(defmethod ev-game-member :ev.g.p/move-cape [x] :ev.g.p/move-cape)
(defmethod ev-game-member :ev.g.p/collect-tile-value [x] :ev.g.p/collect-tile-value)
(defmethod ev-game-member :ev.g.a/finish-game [x] :ev.g.a/finish-game)
(s/def :ev.g.m/event (s/multi-spec ev-game-member :ev/type))

(defmulti ev-game-user (fn [x] (:ev/type x)))
(defmethod ev-game-user :ev.g.u/create [x] :ev.g.u/create)
(defmethod ev-game-user :ev.g.u/delete [x] :ev.g.u/delete)
(defmethod ev-game-user :ev.c/delete-record [x] :ev.c/delete-record)
(defmethod ev-game-user :ev.g.u/configure [x] :ev.g.u/configure)
(defmethod ev-game-user :ev.g.u/start [x] :ev.g.u/start)
(defmethod ev-game-user :ev.g.u/join [x] :ev.g.u/join)
(defmethod ev-game-user :ev.g.u/leave [x] :ev.g.u/leave)
(s/def :ev.g.u/event (s/multi-spec ev-game-user :ev/type))

(defmulti ev-user (fn [x] (:ev/type x)))
(defmethod ev-user :ev.u/create [x] :ev.u/create)
(defmethod ev-user :ev.u/update [x] :ev.u/update)
(defmethod ev-user :ev.u/delete [x] :ev.u/delete)
(s/def :ev.u/event (s/multi-spec ev-user :ev/type))

(comment

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

  (s/explain :ev.g.p/move-cape
             {:ev/type :ev.g.p/move-cape
              :p/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :g/uuid (java.util.UUID/randomUUID)
              :g.p/cape {:g.e/uuid (java.util.UUID/randomUUID)
                       :g.e/type :g.e.type/cape
                       :g.e/pos [1 1]}})

  (s/explain :ev.g.p/event
             {:ev/type :ev.g.p/move-cape
              :p/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :g/uuid (java.util.UUID/randomUUID)
              :g.p/cape {:g.e/uuid (java.util.UUID/randomUUID)
                       :g.e/type :g.e.type/cape
                       :g.e/pos [1 1]}})

  (s/explain :ev.g/event (first (gen/sample gen-ev-p-move-cape 1)))
  (s/explain :ev.g/event (first (gen/sample gen-ev-a-finish-game 1)))

  (s/explain :ev.c/delete-record
             {:ev/type :ev.c/delete-record
              :record/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"})
  (s/explain :ev.c/delete-record
             {:ev/type :ev.c/delete-record})


  (gen/generate (s/gen :ev.g.p/move-cape))

  (gen/generate (s/gen :ev.u/update))

  (s/def ::hello
    (s/with-gen #(clojure.string/includes? % "hello")
      #(gen/fmap (fn [[s1 s2]] (str s1 "hello" s2))
                 (gen/tuple (gen/string-alphanumeric) (gen/string-alphanumeric)))))
  (gen/sample (s/gen ::hello))

  ;;
  )

(comment

  (uuid?  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
  (type #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
  (java.util.UUID/fromString "5ada3765-0393-4d48-bad9-fac992d00e62")

  ;;
  )










