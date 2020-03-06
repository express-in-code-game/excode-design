(ns starnet.app.alpha.streams-test
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :as test :refer [is testing deftest run-tests]]

   [starnet.app.alpha.streams :refer [create-topics list-topics
                                      delete-topics produce-event
                                      future-call-consumer
                                      next-user next-game
                                      send-event create-streams-game create-streams-user]])
  (:import
   org.apache.kafka.clients.consumer.KafkaConsumer
   org.apache.kafka.clients.producer.KafkaProducer))

(comment

  (run-tests)
  (tc/quick-check )

  ;;
  )

(deftest next-game-tests
  (testing "event :ev.g.u/create"
    (is (s/valid? :g/game (next-game nil
                                     (java.util.UUID/randomUUID)
                                     {:ev/type :ev.g.u/create
                                      :u/uuid  (java.util.UUID/randomUUID)}))))
  (testing "random :g/game and :ev.g.u/create event "
    (is (s/valid? :g/game (next-game (sgen/generate (s/gen :g/game))
                                     (java.util.UUID/randomUUID)
                                     (sgen/generate (s/gen :ev.g.u/create)))))))

(deftest all-specchecks
  (testing "running spec.test/check for ns"
    (let [summary (-> (stest/enumerate-namespace 'starnet.app.alpha.streams)
                      (stest/check {:clojure.spec.test.check/opts {:num-tests 10}})
                      (stest/summarize-results))]
      (is (not (contains? summary :check-failed))))))


#_(defn test-ns-hook
    []
    (next-game-stest))