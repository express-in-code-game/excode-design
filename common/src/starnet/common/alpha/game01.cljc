(ns starnet.common.alpha.game01
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.core :refer [make-inst with-gen-fmap]]
   #?(:cljs [starnet.common.alpha.macros :refer-macros [defmethods-for-a-set]]
      :clj  [starnet.common.alpha.macros :refer [defmethods-for-a-set]])))


(comment

  (gen/sample (gen/map gen/keyword gen/boolean) 5)
  (gen/sample (gen/tuple gen/nat gen/boolean gen/ratio))
  (gen/sample (gen/large-integer* {:min 0}))
  (gen/sample (gen/large-integer* {:min 50 :max 100}))
  (gen/sample (gen/large-integer* {:min 50 :max 100}))
  (gen/sample
   (gen/frequency [[5 gen/small-integer] [3 (gen/vector gen/small-integer)] [2 gen/boolean]]))

  (def five-through-nine (gen/choose 5 9))
  (gen/sample five-through-nine)

  (def languages (gen/elements ["clojure" "haskell" "erlang" "scala" "python"]))
  (gen/sample languages)
  (def int-or-nil (gen/one-of [gen/small-integer (gen/return nil)]))
  (gen/sample int-or-nil)
  (def mostly-ints (gen/frequency [[9 gen/small-integer] [1 (gen/return nil)]]))
  (->> (gen/sample mostly-ints 10000) (filter nil?) (count))

  (def even-and-positive (gen/fmap #(* 2 %) gen/nat))
  (gen/sample even-and-positive 20)

  (def powers-of-two (gen/fmap #(int (Math/pow 2 %)) gen/nat))
  (gen/sample powers-of-two)
  (def sorted-vec (gen/fmap sort (gen/vector gen/small-integer)))
  (gen/sample sorted-vec)

  (def anything-but-five (gen/such-that #(not= % 5) gen/small-integer))
  (gen/sample anything-but-five)

  (def vector-and-elem (gen/bind (gen/not-empty (gen/vector gen/small-integer))
                                 #(gen/tuple (gen/return %) (gen/elements %))))
  (gen/sample vector-and-elem)

  (gen/sample (gen/elements [:foo :bar :baz]))
  (gen/sample (gen/elements #{:foo :bar :baz}) 3)
  
  ;;
  )


(def tags #{:entity :cape :knowledge :bio :building :combinable :element})


(s/def :g.e.prop/resolve (s/with-gen int?
                           #(gen/choose 100 1000)))
(s/def :g.e.prop/vision (s/with-gen int?
                          #(gen/choose 4 16)))
(s/def :g.e.prop/energy (s/with-gen int?
                          #(gen/choose 0 100)))
(s/def :g.e/tags (s/with-gen (s/coll-of keyword?)
                   #(gen/list-distinct (gen/elements tags) {:num-elements 3})))

(s/def :g.e/cape (s/keys :req [:g.e.prop/resolve
                               :g.e.prop/vision
                               :g.e.prop/energy
                               :g.e/tags]))

(comment

  (gen/generate (gen/list-distinct (gen/elements tags) {:num-elements 3}))
  (gen/generate (s/gen :g.e/tags))
  (gen/generate (s/gen :g.e/cape))

  ;;
  )
