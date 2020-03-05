(ns app.alpha.tests
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]

   [common.sample-tests]
   [common.alpha.tests]
   [app.sample-tests]
   [app.alpha.streams.game-test]
   [app.alpha.streams.user-test]
   [app.alpha.streams.core-test]
   [app.alpha.spec-test]
   [common.alpha.core :refer [rand-uuid]]))

(deftest common-deps
  (testing "generating random uuid via reader conditionals in .cljc"
    (is (uuid? (rand-uuid)))))

(comment
  
  (do
    (run-tests)
    (run-tests
     'common.sample-tests
     'common.alpha.tests
     'app.sample-tests
     'app.alpha.streams.game-test
     'app.alpha.streams.user-test
     'app.alpha.streams.core-test
     'app.alpha.spec-test))

  (run-all-tests #"app.*")
  (re-matches #"app.*" "app.alpha.streams.game-test")
  (stest/check)
  (tc/quick-check)

  ;;
  )
