(ns app.alpha.streams.user-test
  (:require [app.alpha.streams.user :refer [next-state]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.spec.test.alpha :as stest]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t])
  (:import
   org.apache.kafka.clients.consumer.KafkaConsumer
   org.apache.kafka.clients.producer.KafkaProducer))


(comment

  (run-tests)
  (next-state-speccheck)
  (stest/check `next-state {:clojure.spec.test.check/opts {:num-tests 10}})

  ;;
  )

(deftest next-state-speccheck
  (testing "running spec.test/check"
    (is (every? true?
                (map
                 #(get-in % [:clojure.spec.test.check/ret :pass?])
                 (stest/check `next-state
                              {:clojure.spec.test.check/opts {:num-tests 10}}))))))

(deftest all-specchecks
  (testing "running spec.test/check for ns"
    (is (every? true?
                (map
                 #(get-in % [:clojure.spec.test.check/ret :pass?])
                 (-> (stest/enumerate-namespace 'app.alpha.streams.game)
                     (stest/check {:clojure.spec.test.check/opts {:num-tests 10}})))))))



