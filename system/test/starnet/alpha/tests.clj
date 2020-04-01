(ns starnet.alpha.tests
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]

   
   [starnet.alpha.core.spec]
   [starnet.alpha.spec]

   [starnet.common.sample-tests]
   [starnet.app.sample-tests]
   
   [starnet.alpha.core.tests]
   [starnet.alpha.streams-test]
   [starnet.alpha.spec-test]
   [starnet.alpha.http-test]
   [starnet.alpha.core.tmp :refer [rand-uuid]]))

(defn -main []
  (run-all-tests #"starnet.app.+tests?$|starnet.common.+tests?$")
  (System/exit 0))

(comment

  (stest/check)
  (tc/quick-check)

  (run-tests)
  (run-tests
   'starnet.common.sample-tests
   'starnet.app.sample-tests)

  ;;
  )


