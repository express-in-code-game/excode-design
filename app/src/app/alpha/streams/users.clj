(ns app.alpha.streams.users
  (:require [clojure.pprint :as pp]
            [app.alpha.streams.core :refer [add-shutdown-hook]]
            [clojure.spec.alpha :as s]
            [app.alpha.spec :as spec]
            [clojure.spec.test.alpha :as stest])
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

   java.util.ArrayList
   java.util.Locale
   java.util.Arrays))


(def opts {:base-props {"bootstrap.servers" "broker1:9092"}})
(def state* (atom {:user-data-app nil}))

(defn create-user-data-app
  []
  (let [builder (StreamsBuilder.)
        ktable (-> builder
                   (.stream "alpha.user.data")
                   (.groupByKey)
                   (.aggregate (reify Initializer
                                 (apply [this]
                                   nil))
                               (reify Aggregator
                                 (apply [this k v ag]
                                   (cond
                                     (= v {:delete true}) nil
                                     :else (merge ag v))))
                               (-> (Materialized/as "alpha.user.data.streams.store")
                                   (.withKeySerde (TransitJsonSerde.) #_(Serdes/String))
                                   (.withValueSerde (TransitJsonSerde.))))
                   (.toStream)
                   (.to "alpha.user.data.changes"))
        topology (.build builder)
        props (doto (Properties.)
                (.putAll {"application.id" "alpha.user.data.streams"
                          "bootstrap.servers" "broker1:9092"
                          "auto.offset.reset" "earliest" #_"latest"
                          "default.key.serde" "app.kafka.serdes.TransitJsonSerde"
                          #_(.. Serdes String getClass)
                          "default.value.serde" "app.kafka.serdes.TransitJsonSerde"}))
        streams (KafkaStreams. topology props)
        latch (CountDownLatch. 1)]
    (do
      (add-shutdown-hook props streams latch)
      (.cleanUp streams)
      (.start streams))
    {:builder builder
     :ktable ktable
     :topology topology
     :props props
     :streams streams
     :latch latch}))

(defn mount
  []
  (let [user-data-app (create-user-data-app)]
    (swap! state* assoc :user-data-app user-data-app)))

(defn unmount
  []
  (when (:user-data-app @state*)
    (.close (:streams (:user-data-app @state*)))
    (swap! state* assoc :user-data-app nil)))


(defn produce-event
  [producer topic key event]
  (.send producer (ProducerRecord.
                   topic
                   key
                   event)))
(s/fdef produce-event
  :args (s/cat :producer some? :topic string? :key uuid? :event :event/event))

(defn create-user
  [producer event]
  (produce-event producer
                 "alpha.user.data"
                 (:user/uuid event)
                 event))
; https://clojuredocs.org/clojure.spec.alpha/fdef#example-5c4b535ce4b0ca44402ef629
(s/fdef create-user
  :args (s/cat :producer some? :event :event/create-user))

(comment

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "app.kafka.serdes.TransitJsonSerializer"
                  #_"org.apache.kafka.common.serialization.StringSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))

  (produce-event producer
                 "alpha.user.data"
                 #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
                 {:event/type :event/create-user
                  :user/uuid #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
                  :user/email "user0@gmail.com"
                  :user/username "user0"})

  (produce-event producer
                 "alpha.user.data"
                 #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
                 {:event/type :event/update-user
                  :user/username "user0"})

  (create-user producer {:event/type :event/create-user
                         :user/uuid #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
                         :user/email "user0@gmail.com"
                         :user/username "user0"})

  ;;
  )

(comment

  (mount)
  (unmount)

  (def app (:user-data-app @state*))
  (def streams (:streams app))

  (println (.describe (:topology app)))

  (.isRunning (.state streams))
  (.start streams)
  (.close streams)
  (.cleanUp streams)

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "app.kafka.serdes.TransitJsonSerializer"
                  #_"org.apache.kafka.common.serialization.StringSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))

  (def users {0 #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              1 #uuid "179c265a-7f72-4225-a785-2d048d575854"
              2 #uuid "3a3e2d06-3719-4811-afec-0dffdec35543"})

  (.send producer (ProducerRecord.
                   "alpha.user.data"
                   (get users 0)
                   {:uuid #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
                    :email "user0@gmail.com"
                    :username "user0"}))

  (.send producer (ProducerRecord.
                   "alpha.user.data"
                   (get users 1)
                   {:email "user1@gmail.com"
                    :username "user1"}))

  (.send producer (ProducerRecord.
                   "alpha.user.data"
                   (get users 2)
                   {:email "user2@gmail.com"
                    :username "user2"}))

  (.send producer (ProducerRecord.
                   "alpha.user.data"
                   (get users 2)
                   {:delete true}))

  (def readonly-store (.store streams "alpha.user.data.streams.store"
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
                                    #_"org.apache.kafka.common.serialization.StringDeserializer"
                                    "value.deserializer" "app.kafka.serdes.TransitJsonDeserializer"})]
                     (.subscribe consumer (Arrays/asList (object-array ["alpha.user.data.changes"])))
                     (while true
                       (let [records (.poll consumer 1000)]
                         (.println System/out (str "; app.alpha.streams.users polling changes:" (java.time.LocalTime/now)))
                         (doseq [rec records]
                           (println ";")
                           (println (.key rec))
                           (println (.value rec)))))))))

  (future-cancel fu-consumer)


  ;;
  )