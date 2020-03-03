(ns app.alpha.streams.game-test
  (:require [app.alpha.streams.game :refer [next-state]]
            [clojure.pprint :as pp]
            [app.alpha.streams.core :refer [add-shutdown-hook
                                            produce-event
                                            create-user]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.spec.test.alpha :as stest]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
            [app.alpha.data.spec :refer [gen-ev-p-move-cape
                                         gen-ev-a-finish-game]])
  (:import
   org.apache.kafka.clients.consumer.KafkaConsumer
   org.apache.kafka.clients.producer.KafkaProducer))

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




(comment
  
  (run-tests)
  
  (resolve `next-state)
  (resolve 'next-state)
  (type `next-state)
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

  ;;
  )