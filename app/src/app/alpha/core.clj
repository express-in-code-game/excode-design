(ns app.alpha.core
  (:require [clojure.pprint :as pp]
            [app.alpha.spec :as spec]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen])
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

(def topic-evtype-map
  {"alpha.user" #{:ev.u/create :ev.u/update}
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
   :ev.u/update :u/uuid})


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
  :args (s/cat :producer some? :topic string? :k (s/alt :uuid uuid? :string string?) :event :ev/event))

(defn delete-record
  [producer topic k]
  (produce-event
   producer
   topic
   k
   {:ev/type :ev.c/delete-record}))

(defn future-call-consumer
  [{:keys [topic
           key-des
           value-des
           recordf]
    :or {key-des "app.kafka.serdes.TransitJsonDeserializer"
         value-des "app.kafka.serdes.TransitJsonDeserializer"}
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
             (recordf rec)
            )))))))

(defn create-user
  [producer event]
  (produce-event producer
                 "alpha.user.data"
                 (:u/uuid event)
                 event))
; https://clojuredocs.org/clojure.spec.alpha/fdef#example-5c4b535ce4b0ca44402ef629
(s/fdef create-user
  :args (s/cat :producer some? :event :ev.u/create))


(defn event-to-recordkey
  [ev]
  (or
   (-> ev :ev/type evtype-recordkey-map ev)
   (java.util.UUID/randomUUID)))

(defn event-to-topic
  [ev]
  (-> ev :ev/type evtype-topic-map))


#_(defn send-event
    "Send kafka event. Topic is mapped by ev/type."
    {:arglists '([ev producer]
                 [ev topic producer]
                 [ev recordkey topic producer])}
    ([ev producer]
     [:ev :producer]
     (.send producer
            (event-to-topic ev)
            (event-to-recordkey ev)
            ev))
    ([ev topic producer]
     [:ev :topic :producer]
     (.send producer
            topic
            (event-to-recordkey ev)
            ev))
    ([ev k topic producer]
     [:ev :topic :producer]
     (.send producer
            topic
            k
            ev)))

#_(s/fdef send-event
    :args (s/alt :1 (s/cat :ev :ev/event
                           :producer :instance/producer)
                 :2 (s/cat :ev :ev/event
                           :topic string?
                           :producer :instance/producer)
                 :3 (s/cat :ev :ev/event
                           :topic string?
                           :k uuid?
                           :producer :instance/producer)))

#_(comment

    (ns-unmap *ns* 'send-event)
    (stest/instrument `send-event)
    (stest/unstrument `send-event)

    (def ev (first (gen/sample (s/gen :ev/event) 1)))

    (instance? (resolve 'org.apache.kafka.clients.producer.KafkaProducer) nil)
    (resolve 'org.apache.kafka.clients.producer.KafkaProducer)
    (send-event ev {})
    (send-event ev "asd" nil)
    (send-event ev)

  ;;
    )

(defmulti send-event
  "Send kafka event. Topic is mapped by ev/type."
  {:arglists '([ev kproducer]
               [ev topic kproducer]
               [ev uuidkey kproducer]
               [ev topic uuidkey kproducer])}
  (fn [ev & args]
    (mapv type (into [ev] args))))

(defmethod send-event [Object :isa/producer] 
  [ev kproducer] 
  [:ev :kproducer])
(defmethod send-event [Object String :isa/producer] 
  [ev topic kproducer] 
  [:ev :topic :kproducer])
(defmethod send-event [Object :isa/uuid :isa/producer]
  [ev uuidkey kproducer]
  [:ev :uuidkey :kproducer])
(defmethod send-event [Object String :isa/uuid :isa/producer]
  [ev topic uuidkey kproducer]
  [:ev :topic :uuidkey :kproducer])

(comment

  (ns-unmap *ns* 'send-event)

  (def producer (KafkaProducer.
                 {"bootstrap.servers" "broker1:9092"
                  "auto.commit.enable" "true"
                  "key.serializer" "app.kafka.serdes.TransitJsonSerializer"
                  "value.serializer" "app.kafka.serdes.TransitJsonSerializer"}))
  (def ev (first (gen/sample (s/gen :ev/event) 1)))

  (isa? (class producer) :isa/producer)
  (send-event ev producer)
  (send-event ev "a-topic" producer)
  (send-event ev (java.util.UUID/randomUUID) producer)
  (send-event ev "a-topic" (java.util.UUID/randomUUID) producer)


  (type (java.util.UUID/randomUUID))
  (class (java.util.UUID/randomUUID))
  (= (type (java.util.UUID/randomUUID)) (class (java.util.UUID/randomUUID)))

  (class 1)
  (type "")
  (type {})
  (isa? nil Object)
  (ancestors (class nil))
  (ancestors (class {}))

  ;;
  )







