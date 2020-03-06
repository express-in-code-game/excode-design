(ns starnet.common.sample-tests
  (:require
   [clojure.set :refer [subset?]]
   [clojure.walk :as walk]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test :as test :refer [is run-all-tests testing deftest run-tests]]))

(comment

  (run-tests)
  (spec-tests)
  (sort-is-idempotent)
  (tc/quick-check 100 sort-is-idempotent-prop)
  (stest/check)

  ;;
  )

(deftest test-numbers
  (is (= 1 1)))

(deftest spec-tests
  (testing "with-gen specs"
    (is (let [hello-spec (s/with-gen #(clojure.string/includes? % "hello")
                           #(sgen/fmap
                             (fn [[s1 s2]] (str s1 "hello" s2))
                             (gen/tuple (sgen/string-alphanumeric) (sgen/string-alphanumeric))))]
          (s/valid? (s/coll-of string?) (sgen/sample (s/gen hello-spec))))
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

(def sort-is-idempotent-prop
  (prop/for-all [v (gen/vector gen/int)]
                (= (sort v) (sort (sort v)))))

(defspec sort-is-idempotent 10
  (prop/for-all [v (gen/vector gen/int)]
                (= (sort v) (sort (sort v)))))

(deftest clojure-samples
  (testing "clojure clojure.walk"
    (let [v {:a 1 :b {:c 1}}
          vnext {:a 2 :b {:c 2}}]
      (is (= vnext (walk/postwalk (fn [x]
                                    (if (number? x) (inc x)
                                        x)) v))))))

