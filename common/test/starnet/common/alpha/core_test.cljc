(ns starnet.common.alpha.core-test
  (:require
   [clojure.set :refer [subset?]]
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
   [starnet.common.alpha.core :refer [with-gen-cyclic
                                      rand-uuid
                                      make-inst]]))


(comment
  
  (run-tests)
  
  ;;
  )

(deftest core-tests
  (testing "with-gen-cyclic "
    (is (let [changes {:some "value"}
              s (with-gen-cyclic (s/map-of keyword? string?)
                  (fn [spec]
                    (prn spec)
                    (gen/fmap (fn [x]
                                (merge x changes)) (s/gen spec))))
              v (gen/generate (s/gen s))]
          (subset? (set changes) (set v)))
        "with-gen-cyclic works"))
  (testing "reader conditionals functions from core.cljc"
    (is (uuid? (rand-uuid))
        "rand-uuid returns #uuid")
    (is (inst? (make-inst))
        "make-inst returns #inst")))

