(ns app.alpha.streams.user
  (:require [clojure.pprint :as pp]
            [app.alpha.streams.core :refer [add-shutdown-hook
                                            produce-event
                                            create-user]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
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

(defmulti next-state 
  "Returns next state of the user record"
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-state [:ev.u/create]
  [state k ev]
  (or state ev))

(defmethod next-state [:ev.u/update]
  [state k ev]
  (merge state ev))

(defmethod next-state [:ev.u/delete]
  [state k ev]
  nil)

(s/fdef next-state
  :args (s/cat :state (s/nilable :u/user)
               :k uuid?
               :ev :ev.u/event #_(s/alt :ev.p/move-cape :ev.a/finish-game)))

(defn create-streams-user
  []
  (let [builder (StreamsBuilder.)
        ktable (-> builder
                   (.stream "alpha.user")
                   (.groupByKey)
                   (.aggregate (reify Initializer
                                 (apply [this]
                                   nil))
                               (reify Aggregator
                                 (apply [this k v ag]
                                        (next-state ag k v)))
                               (-> (Materialized/as "alpha.user.streams.store")
                                   (.withKeySerde (TransitJsonSerde.))
                                   (.withValueSerde (TransitJsonSerde.))))
                   (.toStream)
                   (.to "alpha.user.changes"))
        topology (.build builder)
        props (doto (Properties.)
                (.putAll {"application.id" "alpha.user.streams"
                          "bootstrap.servers" "broker1:9092"
                          "auto.offset.reset" "earliest" #_"latest"
                          "default.key.serde" "app.kafka.serdes.TransitJsonSerde"
                          "default.value.serde" "app.kafka.serdes.TransitJsonSerde"}))
        streams (KafkaStreams. topology props)
        latch (CountDownLatch. 1)]
    (do
      (add-shutdown-hook props streams latch))
    {:builder builder
     :ktable ktable
     :topology topology
     :props props
     :streams streams
     :latch latch}))


(defn create-streams-user-games
  []
  ; ktable of :u/uuid -> (list-of :g/uuids )
  ; request for list of user games will read from the store using uuids
  ; client will sort/group by host/player/observer
  )
