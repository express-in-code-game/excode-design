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
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]))

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
                        (fn [v]
                          (gen/fmap (fn [x]
                                      (merge
                                       x
                                       changes)) (gen/return v)))))
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

(defprotocol P
  (bar [this a] [this a b] "bar doc")
  (baz [this x] "baz doc"))

(extend-protocol P
  #?(:clj clojure.lang.Keyword
     :cljs cljs.core/Keyword)
  (bar ([k a] [k a])
    ([k a b] [k a b]))
  (baz [k a] [k a])
  #?(:clj clojure.lang.Symbol
     :cljs cljs.core/Symbol)
  (bar ([s a] [s a])
    ([s a b] [s a b]))
  (baz [s a] [s a]))

(deftest protocol-1
  (let [x (reify
            P
            (bar [_ a] [a])
            (bar [_ a b] [a b])
            (baz [_ x] [x]))]
    (are [x y] (= x y)
      (bar x 1) [1]
      (bar x 3 4) [3 4]
      (baz x 5) [5]
      (bar :a 1) [:a 1]
      (bar :a 1 2) [:a 1 2]
      (baz :a 1) [:a 1]
      (bar 'a 1) ['a 1]
      (bar 'a 1 2) ['a 1 2]
      (baz 'a 1) ['a 1])))

(comment
  
  (clojure.set/intersection (ancestors (type {})) (ancestors (type #{})))
  
  (clojure.set/intersection (ancestors (type {}))
                            (ancestors (type #{}))
                            (ancestors (type [])))
  ;;
  )

