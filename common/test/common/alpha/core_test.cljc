(ns common.alpha.core-test
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
   [common.alpha.core :refer [with-gen-cyclic]]))


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
              vl (gen/generate (s/gen s))]
          (subset? (set changes) (set vl)))
        "with-gen-cyclic works")))
