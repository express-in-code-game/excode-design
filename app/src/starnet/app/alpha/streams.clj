(ns starnet.app.alpha.streams
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]

   [starnet.common.alpha.spec]
   [starnet.app.alpha.spec]
   [starnet.common.alpha.data :refer [make-default-game-state]])
  (:import
   starnet.app.alpha.aux.serdes.TransitJsonSerializer
   starnet.app.alpha.aux.serdes.TransitJsonDeserializer
   starnet.app.alpha.aux.serdes.TransitJsonSerde

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

(defn create-topics
  [{:keys [props
           names
           num-partitions
           replication-factor] :as opts}]
  (let [client (KafkaAdminClient/create props)
        topics (java.util.ArrayList.
                (mapv (fn [name]
                        (NewTopic. name num-partitions (short replication-factor))) names))]
    (.createTopics client topics)))

(defn delete-topics
  [{:keys [props
           names] :as opts}]
  (let [client  (KafkaAdminClient/create props)]
    (.deleteTopics client (java.util.ArrayList. names))))

(defn list-topics
  [{:keys [props] :as opts}]
  (let [client (KafkaAdminClient/create props)
        kfu (.listTopics client)]
    (.. kfu (names) (get))))

(defn add-shutdown-hook
  [props streams latch]
  (-> (Runtime/getRuntime)
      (.addShutdownHook (proxy
                         [Thread]
                         ["streams-shutdown-hook"]
                          (run []
                            (when (.isRunning (.state streams))
                              (.println (System/out))
                              (println "; closing" (.get props "application.id"))
                              (.close streams))
                            (.countDown latch))))))

(defn produce-event
  [producer topic k event]
  (.send producer (ProducerRecord.
                   topic
                   k
                   event)))

(s/fdef produce-event
  :args (s/cat :producer :instance/kproducer
               :topic string?
               :k (s/alt :uuid uuid? :string string?)
               :event :ev/event))

(defn future-call-consumer
  [{:keys [topic
           key-des
           value-des
           recordf]
    :or {key-des "starnet.app.alpha.aux.serdes.TransitJsonDeserializer"
         value-des "starnet.app.alpha.aux.serdes.TransitJsonDeserializer"}
    :as opts}]
  (future-call
   (fn []
     (let [consumer (KafkaConsumer.
                     {"bootstrap.servers" "broker1:9092"
                      "auto.offset.reset" "earliest"
                      "auto.commit.enable" "false"
                      "group.id" (.toString (java.util.UUID/randomUUID))
                      "consumer.timeout.ms" "5000"
                      "key.deserializer" key-des
                      "value.deserializer" value-des})]
       (.subscribe consumer (Arrays/asList (object-array [topic])))
       (while true
         (let [records (.poll consumer 1000)]
           (.println System/out (str "; polling " topic (java.time.LocalTime/now)))
           (doseq [rec records]
             (recordf rec))))))))

(defn create-streams
  [appid topology-fn]
  (let [builder (StreamsBuilder.)
        stream (topology-fn builder)
        topology (.build builder)
        props (doto (Properties.)
                (.putAll {"application.id" appid
                          "bootstrap.servers" "broker1:9092"
                          "auto.offset.reset" "earliest" #_"latest"
                          "default.key.serde" "starnet.app.alpha.aux.serdes.TransitJsonSerde"
                          "default.value.serde" "starnet.app.alpha.aux.serdes.TransitJsonSerde"}))
        kstreams (KafkaStreams. topology props)
        latch (CountDownLatch. 1)]
    (do
      (add-shutdown-hook props kstreams latch))
    {:builder builder
     :stream stream
     :topology topology
     :props props
     :kstreams kstreams
     :latch latch}))

(def topic-evtype-map
  {"alpha.user" #{:ev.u/create :ev.u/update :ev.u/delete}
   "alpha.game" #{:ev.g.u/create :ev.g.u/delete
                  :ev.g.u/join :ev.g.u/leave
                  :ev.g.u/configure :ev.g.u/start
                  :ev.g.p/move-cape :ev.g.p/collect-tile-value
                  :ev.g.a/finish-game}})

(def evtype-topic-map
  (->> topic-evtype-map
       (map (fn [[topic kset]]
              (map #(vector % topic) kset)))
       (mapcat identity)
       (into {})))

(def evtype-recordkey-map
  {:ev.u/create :u/uuid
   :ev.u/update :u/uuid
   :ev.u/delete :u/uuid
   :ev.g.u/configure :g/uuid})

(defn event-to-recordkey
  [ev]
  (or
   (-> ev :ev/type evtype-recordkey-map ev)
   (java.util.UUID/randomUUID)))

(defn event-to-topic
  [ev]
  (-> ev :ev/type evtype-topic-map))

(defmulti send-event
  "Send kafka event. Topic is mapped by ev/type."
  {:arglists '([ev kproducer]
               [ev topic kproducer]
               [ev uuidkey kproducer]
               [ev topic uuidkey kproducer])}
  (fn [ev & args]
    (mapv type (into [ev] args))))

(defmethod send-event [Object :isa/kproducer]
  [ev kproducer]
  (.send kproducer
         (ProducerRecord.
          (event-to-topic ev)
          (event-to-recordkey ev)
          ev)))

(defmethod send-event [Object String :isa/kproducer]
  [ev topic kproducer]
  (.send kproducer
         (ProducerRecord.
          topic-evtype-map
          (event-to-recordkey ev)
          ev)))

(defmethod send-event [Object :isa/uuid :isa/kproducer]
  [ev uuidkey kproducer]
  (.send kproducer
         (ProducerRecord.
          (event-to-topic ev)
          uuidkey
          ev)))

(defmethod send-event [Object String :isa/uuid :isa/kproducer]
  [ev topic uuidkey kproducer]
  (.send kproducer
         (ProducerRecord.
          topic
          uuidkey
          ev)))


(defmulti next-user
  "Returns next state of the user record"
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-user [:ev.u/create]
  [state k ev]
  (or state ev))

(defmethod next-user [:ev.u/update]
  [state k ev]
  (when state
    (merge state ev)))

(defmethod next-user [:ev.u/delete]
  [state k ev]
  nil)

(s/fdef next-user
  :args (s/cat :state (s/nilable :u/user)
               :k uuid?
               :ev :ev.u/event #_(s/alt :ev.p/move-cape :ev.a/finish-game)))

(comment

  (ns-unmap *ns* 'next-user)
  (stest/instrument [`next-user])
  (stest/unstrument [`next-user])

  ;;
  )

(defn create-streams-user
  []
  (create-streams "alpha.user.streams"
                  (fn [builder]
                    (-> builder
                        (.stream "alpha.user")
                        (.groupByKey)
                        (.aggregate (reify Initializer
                                      (apply [this]
                                        nil))
                                    (reify Aggregator
                                      (apply [this k v ag]
                                        (next-user ag k v)))
                                    (-> (Materialized/as "alpha.user.streams.store")
                                        (.withKeySerde (TransitJsonSerde.))
                                        (.withValueSerde (TransitJsonSerde.))))
                        (.toStream)
                        (.to "alpha.user.changes")))))


(defmulti next-game
  "Returns the next state of the game."
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-game [:ev.c/delete-record]
  [state k ev]
  nil)

(defmethod next-game [:ev.g.u/create]
  [state k ev]
  (or
   state
   (make-default-game-state k ev)))

(defmethod next-game [:ev.g.u/delete]
  [state k ev]
  nil)

(defmethod next-game [:ev.g.u/configure]
  [state k ev]
  (when state
    (merge state ev)))

(defmethod next-game [:ev.g.u/start]
  [state k ev]
  state)

(defmethod next-game [:ev.g.u/join]
  [state k ev]
  state)

(defmethod next-game [:ev.g.u/leave]
  [state k ev]
  state)

(defmethod next-game [:ev.g.p/move-cape]
  [state k ev]
  state)

(defmethod next-game [:ev.g.a/finish-game]
  [state k ev]
  state)

(defmethod next-game [:ev.g.p/collect-tile-value]
  [state k ev]
  state)

(s/fdef next-game
  :args (s/cat :state (s/nilable :g/game)
               :k uuid?
               :ev :ev.g/event #_(s/alt :ev.p/move-cape :ev.a/finish-game))
  :ret (s/nilable :g/game))

(comment

  (ns-unmap *ns* 'next-game)
  (stest/instrument [`next-game])
  (stest/unstrument [`next-game])

  ;;
  )

(defn assert-next-game-post [state] {:post [(s/assert :g/game %)]} state)
(defn assert-next-game-body [state]
  (let [data (s/conform :g/game state)]
    (if (= data ::s/invalid)
      (throw (ex-info "Invalid data"
                      (select-keys (s/explain-data :g/game state) [::s/spec ::s/problems])))
      data)))

(comment

  (def a-game (sgen/generate (s/gen :g/game)))
  (def ev {:ev/type :ev.g.u/configure
           :u/uuid (java.util.UUID/randomUUID)
           :g/uuid (java.util.UUID/randomUUID)
           :g/status :opened})
  (def nv (next-state a-game (java.util.UUID/randomUUID) ev))
  (def anv (assert-next-game-post nv))
  (def anv (assert-next-game-post a-game))
  (def anv (assert-next-game-body nv))
  (s/explain :g/game nv)
  (s/assert :g/game nv)
  (s/assert :g/game a-game)
  (s/check-asserts?)
  (s/check-asserts true)

  (keys (s/explain-data :g/game nv))
  (s/conform :g/game nv)

  (try
    (assert-next-game-body nv)
    (catch Exception e
      #_(println e)
      (println (ex-message e))
      (println (ex-data e))))
  ;;
  )

(defn create-streams-game
  []
  (create-streams "alpha.game.streams"
   (fn [builder]
     (-> builder
         (.stream "alpha.game")
         (.groupByKey)
         (.aggregate (reify Initializer
                       (apply [this]
                         nil))
                     (reify Aggregator
                       (apply [this k v ag]
                         (try
                           (assert-next-game-body (next-game ag k v))
                           (catch Exception e
                             (println (ex-message e))
                             (println (ex-data e))
                             ag))))
                     (-> (Materialized/as "alpha.game.streams.store")
                         (.withKeySerde (TransitJsonSerde.))
                         (.withValueSerde (TransitJsonSerde.))))
         (.toStream)
         (.to "alpha.game.changes")))))



(defn next-user-games [])