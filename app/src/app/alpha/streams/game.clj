(ns app.alpha.streams.game
  (:require [clojure.pprint :as pp]
            [app.alpha.streams.core :refer [add-shutdown-hook
                                            produce-event
                                            create-user]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [app.alpha.spec :refer [gen-ev-p-move-cape
                                    gen-ev-a-finish-game]])
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


(defn gen-default-game-state
  [k ev]
  (let [host-uuid (:u/uuid ev)]
    {:g/uuid k
     :g/status :created
     :g/start-inst (java.util.Date.)
     :g/duration-ms 60000
     :g/map-size [16 16]
     :g/roles {host-uuid {:g.r/host true
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
                        (vec))}))

(defmulti next-state
  "Returns the next state of the game."
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-state [:ev.c/delete-record]
  [state k ev]
  nil)

(defmethod next-state [:ev.g.u/create]
  [state k ev]
  (or
   state
   (gen-default-game-state k ev)))

(defmethod next-state [:ev.g.u/delete]
  [state k ev]
  nil)

(defmethod next-state [:ev.g.u/configure]
  [state k ev]
  (merge state ev {:g/uuid "hello"}))

(defmethod next-state [:ev.g.u/start]
  [state k ev]
  state)

(defmethod next-state [:ev.g.u/join]
  [state k ev]
  state)

(defmethod next-state [:ev.g.u/leave]
  [state k ev]
  state)

(defmethod next-state [:ev.g.p/move-cape]
  [state k ev]
  state)

(defmethod next-state [:ev.g.a/finish-game]
  [state k ev]
  state)

(s/fdef next-state
    :args (s/cat :state (s/nilable :g/game)
                 :k uuid?
                 :ev :ev.g/event #_(s/alt :ev.p/move-cape :ev.a/finish-game)))


(defn assert-next-state [state] {:post [(s/assert :g/game %)]} state)

(comment
  (gensym "tmp")
  
  (ns-unmap *ns* 'next-state)

  (stest/instrument [`next-state])
  (stest/unstrument [`next-state])

  (def state (gen/generate (s/gen :g/game)))
  (def ev (first (gen/sample (s/gen :ev.g.u/create) 1)))
  (s/explain :g/game (gen-default-game-state (java.util.UUID/randomUUID) ev))

  (def ev-p (gen/generate (s/gen :ev.p/move-cape)))
  (def ev-a (gen/generate (s/gen :ev.a/finish-game)))

  (def ev-p (first (gen/sample gen-ev-p-move-cape 1)))
  (def ev-a (first (gen/sample gen-ev-a-finish-game 1)))

  (next-state state ev-p)
  (next-state state ev-a)

  (next-state state (merge ev-p {:p/uuid "asd"}))


  (def a-game (gen/generate (s/gen :g/game)))
  (def ev {:ev/type :ev.g.u/configure
           :u/uuid (java.util.UUID/randomUUID)
           :g/uuid (java.util.UUID/randomUUID)
           :g/status :opened})
  (def nv (next-state a-game (java.util.UUID/randomUUID) ev))
  (def anv (assert-next-state nv))
  (def anv (assert-next-state a-game))
  (s/explain :g/game nv)
  (s/explain-data :g/game nv)
  (s/assert :g/game nv)
  (s/assert :g/game a-game)
  (s/check-asserts?)
  (s/check-asserts true)
  
  (try
    (s/assert :g/game nv)
    (catch Exception e
      #_(println e)
      (println (ex-message e))
      #_(println (ex-data e))
      #_(println e)))
  ;;
  )

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
                                   (println "; call create-streams-game aggregate ")
                                   (println (type k))
                                   (println (type v))
                                   (println (type ag))
                                   (try
                                     (assert-next-state (next-state ag k v))
                                     (catch Exception e
                                       (println (ex-message e))
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
                  "key.serializer" "app.kafka.serdes.TransitJsonSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))

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
