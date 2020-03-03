(ns app.alpha.streams.user-test
  (:require [app.alpha.streams.user :refer [next-state]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.spec.test.alpha :as stest]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [app.alpha.data.spec :refer [gen-ev-p-move-cape
                                         gen-ev-a-finish-game]])
  (:import
   org.apache.kafka.clients.consumer.KafkaConsumer
   org.apache.kafka.clients.producer.KafkaProducer))

(s/fdef next-state
  :args (s/cat :state (s/nilable :u/user)
               :k uuid?
               :ev :ev.u/event #_(s/alt :ev.p/move-cape :ev.a/finish-game)))

(comment

  (ns-unmap *ns* 'next-state)

  (stest/instrument [`next-state])
  (stest/unstrument [`next-state])

  (def state (gen/generate (s/gen :g/game)))
  (def u (gen/generate (s/gen :u/user)))

  (def ev-p (gen/generate (s/gen :ev.p/move-cape)))
  (def ev-a (gen/generate (s/gen :ev.a/finish-game)))

  (def ev-p (first (gen/sample gen-ev-p-move-cape 1)))
  (def ev-a (first (gen/sample gen-ev-a-finish-game 1)))

  (next-state state ev-p)
  (next-state state ev-a)

  (next-state state (merge ev-p {:p/uuid "asd"}))

  ;;
  )

(comment

  (def state (create-streams-user))
  (def streams (:streams state))

  (println (.describe (:topology state)))

  (.isRunning (.state streams))
  (.start streams)
  (.close streams)
  (.cleanUp streams)

  (def users {0 #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              1 #uuid "179c265a-7f72-4225-a785-2d048d575854"
              2 #uuid "3a3e2d06-3719-4811-afec-0dffdec35543"})

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "app.kafka.serdes.TransitJsonSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))

  (create-user producer {:event/type :event/create-user
                         :user/uuid (get users 0)
                         :user/email "user0@gmail.com"
                         :user/username "user0"})

  (create-user producer {:event/type :event/create-user
                         :user/uuid (get users 1)
                         :user/email "user1@gmail.com"
                         :user/username "user1"})

  (create-user producer {:event/type :event/create-user
                         :user/uuid (get users 2)
                         :user/email "user2@gmail.com"
                         :user/username "user2"})

  (produce-event producer
                 "alpha.user"
                 (get users 0)
                 {:event/type :event/update-user
                  :user/username "user0"})

  (produce-event producer
                 "alpha.user"
                 (get users 0)
                 {:event/type :event/delete-record})
  (produce-event producer
                 "alpha.user"
                 (get users 1)
                 {:event/type :event/delete-record})
  (produce-event producer
                 "alpha.user"
                 (get users 2)
                 {:event/type :event/delete-record})



  (def readonly-store (.store streams "alpha.user.streams.store"
                              (QueryableStoreTypes/keyValueStore)))

  (.approximateNumEntries readonly-store)
  (count (iterator-seq (.all readonly-store)))

  (doseq [x (iterator-seq (.all readonly-store))]
    (println (.key x) (.value x)))

  (.get readonly-store (get users 0))
  (.get readonly-store (get users 1))
  (.get readonly-store (get users 2))
  (type (:uuid (.get readonly-store (get users 0))))
  (=  #uuid "3a3e2d06-3719-4811-afec-0dffdec35543"  #uuid "3a3e2d06-3719-4811-afec-0dffdec35543")

  (def fu-consumer
    (future-call (fn []
                   (let [consumer (KafkaConsumer.
                                   {"bootstrap.servers" "broker1:9092"
                                    "auto.offset.reset" "earliest"
                                    "auto.commit.enable" "false"
                                    "group.id" (.toString (java.util.UUID/randomUUID))
                                    "consumer.timeout.ms" "5000"
                                    "key.deserializer"
                                    "app.kafka.serdes.TransitJsonDeserializer"
                                    "value.deserializer" "app.kafka.serdes.TransitJsonDeserializer"})]
                     (.subscribe consumer (Arrays/asList (object-array ["alpha.user.changes"])))
                     (while true
                       (let [records (.poll consumer 1000)]
                         (.println System/out (str "; polling alpha.user.changes " (java.time.LocalTime/now)))
                         (doseq [rec records]
                           (println ";")
                           (println (.key rec))
                           (println (.value rec)))))))))

  (future-cancel fu-consumer)

  ;;
  )