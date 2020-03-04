(ns app.alpha.spec-test
  (:require
   [app.alpha.spec]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t])
  (:import
   java.util.Date
   org.apache.kafka.clients.producer.KafkaProducer
   org.apache.kafka.clients.consumer.KafkaConsumer))

(comment
  
  (run-tests)
  
  ;;
  )

(deftest sample-tests
  (testing "relationships created with (derive )"
    (is (isa? org.apache.kafka.clients.producer.KafkaProducer :isa/kproducer)
        "KafkaProducer is a :isa/kproducer")))