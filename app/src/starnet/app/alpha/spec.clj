(ns starnet.app.alpha.spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest])
  (:import
   java.util.Date
   org.apache.kafka.clients.producer.KafkaProducer
   org.apache.kafka.clients.consumer.KafkaConsumer))

(derive java.util.Map :isa/map)
(derive java.util.Set :isa/set)
(derive java.util.UUID :isa/uuid)
(derive org.apache.kafka.clients.producer.KafkaProducer :isa/kproducer)
(derive org.apache.kafka.clients.consumer.KafkaConsumer :isa/kconsumer)
(derive clojure.lang.Keyword :isa/keyword)

(s/def :instance/kproducer #(instance? org.apache.kafka.clients.producer.KafkaProducer %))

(s/def :instance/kconsumer #(instance? org.apache.kafka.clients.consumer.KafkaConsumer %))


(s/fdef starnet.app.alpha.streams/produce-event
  :args (s/cat :producer :instance/kproducer
               :topic string?
               :k (s/alt :uuid uuid? :string string?)
               :event :ev/event))

(s/fdef starnet.app.alpha.streams/next-user
  :args (s/cat :state (s/nilable :u/user)
               :k uuid?
               :ev :ev.u/event)
  :ret (s/nilable :u/user))

(s/fdef starnet.app.alpha.streams/next-game
  :args (s/cat :state (s/nilable :g/game)
               :k uuid?
               :ev :ev.g/event )
  :ret (s/nilable :g/game))