(ns common.alpha.spec-test
  (:require
   [clojure.set :refer [subset?]]
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
   [common.alpha.spec :refer [setof-ev-event]]))


(comment
  
  (run-tests)
  
  ;;
  )

(deftest spec-tests
  (is (s/valid? (s/coll-of :u/email) (gen/sample (s/gen :u/email)))
      "coll-of :u/email can be generated via custom gen")
  (testing ":g/game spec"
    (is (s/valid? :g/game (gen/generate (s/gen :g/game)))
        "game can be generated"))
  (testing "event specs"
    (is (subset? (into #{} (gen/sample (s/gen :ev/type))) setof-ev-event)
        "generate a subset of :ev/type")))

(deftest sample-tests
  (testing "with-gen specs"
    (is (let [hello-spec (s/with-gen #(clojure.string/includes? % "hello")
                           #(gen/fmap
                             (fn [[s1 s2]] (str s1 "hello" s2))
                             (gen/tuple (gen/string-alphanumeric) (gen/string-alphanumeric))))]
          (s/valid? (s/coll-of string?) (gen/sample (s/gen hello-spec))))
        "hello spec custom gen example")
    (is (let [spec (s/map-of keyword? string?)
              changes {:some-key "some value"}
              nspec (s/with-gen spec
                      #(gen/bind
                        (s/gen spec)
                        (fn [vl]
                          (gen/fmap (fn [x]
                                      (merge
                                       x
                                       changes)) (gen/return vl)))))
              nvl (gen/generate (s/gen nspec))]
          (subset? (set changes) (set nvl)))
        "change (with-gen)spec generated value using gen/bind")
    (is (let [spec (s/map-of keyword? string?)
              changes {:some-key "some value"}
              nspec (s/with-gen spec
                      #(gen/fmap (fn [x]
                                   (merge
                                    x
                                    changes)) (s/gen spec)))
              nvl (gen/generate (s/gen nspec))]
          (subset? (set changes) (set nvl)))
        "change (with-gen)spec generated value using gen/fmap directly")))

