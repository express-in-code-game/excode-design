(ns starnet.app.alpha.streams.game
  (:require
   [clojure.pprint :as pp]
   [starnet.app.alpha.streams.core :refer [add-shutdown-hook
                                   produce-event
                                   create-user]]
   [starnet.common.alpha.data :refer [make-default-game-state]]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [starnet.app.alpha.streams.game-testfn :refer [assert-next-state-body]])
  (:import
   starnet.app.aux.serdesTransitJsonSerializer
   starnet.app.aux.serdesTransitJsonDeserializer
   starnet.app.aux.serdesTransitJsonSerde
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




(defn create-streams-game
  []
  (let [builder (StreamsBuilder.)
        ktable (-> builder
                   (.stream "alpha.game")
                   (.groupByKey)
                   (.aggregate (reify Initializer
                                 (apply [this]
                                   nil))
                               (reify Aggregator
                                 (apply [this k v ag]
                                        (try
                                          (assert-next-state-body (next-state ag k v))
                                          (catch Exception e
                                            (println (ex-message e))
                                            (println (ex-data e))
                                            ag))))
                               (-> (Materialized/as "alpha.game.streams.store")
                                   (.withKeySerde (TransitJsonSerde.))
                                   (.withValueSerde (TransitJsonSerde.))))
                   (.toStream)
                   (.to "alpha.game.changes"))
        topology (.build builder)
        props (doto (Properties.)
                (.putAll {"application.id" "alpha.game.streams"
                          "bootstrap.servers" "broker1:9092"
                          "auto.offset.reset" "earliest" #_"latest"
                          "default.key.serde" "starnet.app.aux.serdesTransitJsonSerde"
                          "default.value.serde" "starnet.app.aux.serdesTransitJsonSerde"}))
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

(comment


  (def state (create-streams-game))
  (def streams (:streams state))

  (println (.describe (:topology state)))

  (.isRunning (.state streams))
  (.start streams)
  (.close streams)
  (.cleanUp streams)

  (isa? (class {}) java.util.Map)


  (def players {0 #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
                1 #uuid "179c265a-7f72-4225-a785-2d048d575854"})

  (def observers {0 #uuid "46855899-838a-45fd-98b4-c76c08954645"
                  1 #uuid "ea1162e3-fe45-4652-9fa9-4f8dc6c78f71"
                  2 #uuid "4cd4b905-6859-4c22-bae7-ad5ec51dc3f8"})

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "starnet.app.aux.serdesTransitJsonSerializer"
                  "value.serializer" "starnet.app.aux.serdesTransitJsonSerializer"}))

  (produce-event producer
                 "alpha.game"
                 (get players 0)
                 {:ev/type :ev.p/move-cape
                  :p/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
                  :g/uuid (java.util.UUID/randomUUID)
                  :p/cape {:g.e/uuid (java.util.UUID/randomUUID)
                           :g.e/type :g.e.type/cape
                           :g.e/pos [1 1]}})

  (produce-event producer
                 "alpha.game"
                 (get players 0)
                 {:ev/type :ev.c/delete-record})

  (def readonly-store (.store streams "alpha.game.streams.store"
                              (QueryableStoreTypes/keyValueStore)))

  (count (iterator-seq (.all readonly-store)))
  (doseq [x (iterator-seq (.all readonly-store))]
    (println (.key x) (.value x)))
  (.get readonly-store (get players 0))

  (java.util.UUID/randomUUID)
  ;;
  )
