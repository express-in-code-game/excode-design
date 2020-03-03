(ns app.alpha.streams.game-test
  (:require [clojure.pprint :as pp]
            [app.alpha.streams.core :refer [add-shutdown-hook
                                            produce-event
                                            create-user]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.spec.test.alpha :as stest]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
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


(s/fdef next-state
  :args (s/cat :state (s/nilable :g/game)
               :k uuid?
               :ev :ev.g/event #_(s/alt :ev.p/move-cape :ev.a/finish-game))
  :ret (s/nilable :g/game))


(defn assert-next-state-post [state] {:post [(s/assert :g/game %)]} state)
(defn assert-next-state-body [state]
  (let [data (s/conform :g/game state)]
    (if (= data ::s/invalid)
      (throw (ex-info "Invalid data"
                      (select-keys (s/explain-data :g/game state) [::s/spec ::s/problems])))
      data)))

(comment

  (stest/check `next-state {:clojure.spec.test.check/opts {:num-tests 1}})
  (stest/summarize-results (stest/check `next-state {:clojure.spec.test.check/opts {:num-tests 1}}))
  (stest/summarize-results (stest/check))
  (-> (stest/enumerate-namespace (ns-name *ns*)) (stest/check {:clojure.spec.test.check/opts {:num-tests 2}}))

  clojure.test/*load-tests*
  (alter-var-root #'clojure.test/*load-tests* (fn [v] true))

  (gensym "tmp")

  (ns-unmap *ns* 'next-state)

  (stest/instrument [`next-state])
  (stest/unstrument [`next-state])

  (def state (sgen/generate (s/gen :g/game)))
  (def ev (first (sgen/sample (s/gen :ev.g.u/create) 1)))
  (s/explain :g/game (gen-default-game-state (java.util.UUID/randomUUID) ev))

  (def ev-p (sgen/generate (s/gen :ev.p/move-cape)))
  (def ev-a (sgen/generate (s/gen :ev.a/finish-game)))

  (def ev-p (first (sgen/sample gen-ev-p-move-cape 1)))
  (def ev-a (first (sgen/sample gen-ev-a-finish-game 1)))

  (next-state state ev-p)
  (next-state state ev-a)

  (next-state state (merge ev-p {:p/uuid "asd"}))


  (def a-game (sgen/generate (s/gen :g/game)))
  (def ev {:ev/type :ev.g.u/configure
           :u/uuid (java.util.UUID/randomUUID)
           :g/uuid (java.util.UUID/randomUUID)
           :g/status :opened})
  (def nv (next-state a-game (java.util.UUID/randomUUID) ev))
  (def anv (assert-next-state-post nv))
  (def anv (assert-next-state-post a-game))
  (def anv (assert-next-state-body nv))
  (s/explain :g/game nv)
  (s/assert :g/game nv)
  (s/assert :g/game a-game)
  (s/check-asserts?)
  (s/check-asserts true)

  (keys (s/explain-data :g/game nv))
  (s/conform :g/game nv)

  (try
    (assert-next-state-body nv)
    (catch Exception e
      #_(println e)
      (println (ex-message e))
      (println (ex-data e))))
  ;;
  )