(ns app.sample-tests
  (:require
   [clojure.set :refer [subset?]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]))

(comment

  (run-tests)
  (run-all-tests #"app.*")
  (re-matches #"app.*" "app.alpha.streams.game-test")
  (stest/check)
  (tc/quick-check)

  ;;
  )

(deftest some-tests
  (testing "time"
    (is (inst? (java.util.Date.))
        "java.util.Date satisfies Inst")
    (is (inst? (java.time.Instant/now))
        "java.time.Instant/now satisfies Inst"))
  (testing "uuid"
    (is (uuid? (java.util.UUID/randomUUID))
        "uuid? java.util.UUID/randomUUID is true")
    (is (= (type #uuid "5ada3765-0393-4d48-bad9-fac992d00e62") java.util.UUID)
        "type of #uuid  is java.util.UUID")
    (is (uuid? (java.util.UUID/fromString "5ada3765-0393-4d48-bad9-fac992d00e62")))
    "java.util.UUID/fromString uuid? true")
  (testing "some data"
    (pr-str (java.util.Date.))
    (let [x {:a 1}]
      (is (= x (read-string (pr-str x)))
          "pr-str then read-string ")))
  (testing "rebinding root vars"
    (is (false? (alter-var-root #'clojure.test/*load-tests* (fn [_] false))))
    (is (true? (alter-var-root #'clojure.test/*load-tests* (fn [_] true))))
    (is (true? clojure.test/*load-tests*)))
  (testing "isa?"
    (is (false? (isa? nil Object))
        "isa? nil Object is false"))
  (testing "stuff"
    (is (= (type (java.util.UUID/randomUUID)) (class (java.util.UUID/randomUUID)))
        "(type) and (class) are same when metadata has no :type or something"))
  (testing "macros"
    (is (= (macroexpand '(when (pos? a) (println "positive") (/ b a)))
           '(if (pos? a) (do (println "positive") (/ b a))))
        "macroexpand example with when macro")))

