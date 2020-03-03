(ns app.alpha.spec
  (:require [app.alpha.data.spec]
            
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