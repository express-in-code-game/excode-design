(ns starnet.app.alpha.streams.game-test
  (:require
   [starnet.app.alpha.streams.game :refer [next-state]]
   [clojure.pprint :as pp]
   [starnet.app.alpha.streams.core :refer [add-shutdown-hook
                                   produce-event
                                   create-user]]
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

  (tc/quick-check 10 sort-is-idempotent-prop)

  (stest/check `next-state {:clojure.spec.test.check/opts {:num-tests 1000}})
  (stest/summarize-results (stest/check `next-state
                                        {:clojure.spec.test.check/opts {:num-tests 1}}))
  (stest/summarize-results (stest/check))
  (-> (stest/enumerate-namespace 'starnet.app.alpha.streams.game)
      (stest/check {:clojure.spec.test.check/opts {:num-tests 2}}))

  ;;
  )

(deftest next-state-tests
  (testing "event :ev.g.u/create"
    (is (s/valid? :g/game (next-state nil
                                      (java.util.UUID/randomUUID)
                                      {:ev/type :ev.g.u/create
                                       :u/uuid  (java.util.UUID/randomUUID)}))))
  (testing "random :g/game and :ev.g.u/create event "
    (is (s/valid? :g/game (next-state (sgen/generate (s/gen :g/game))
                                      (java.util.UUID/randomUUID)
                                      (first (sgen/generate (s/gen :ev.g.u/create))))))))

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
                 (-> (stest/enumerate-namespace 'starnet.app.alpha.streams.game)
                     (stest/check {:clojure.spec.test.check/opts {:num-tests 10}})))))))

#_(defn test-ns-hook
    []
    (next-state-stest))
