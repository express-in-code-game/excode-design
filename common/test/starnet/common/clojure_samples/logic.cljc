(ns starnet.common.clojure-samples.logic
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.logic :as l]))

(comment

  (l/run* [q]
          (l/== q true))

  (l/run* [q]
          (l/membero q '(:cat :dog :bird :bat :zebra)))

  (def a {:type :some-entity
          :tags #{:t1 :t2 :t3}
          :logic-alias :a})

  (def b {:type :some-entity
          :tags #{:t2 :t3}
          :logic-alias :a})

  (def c {:type :some-entity
          :tags #{:t1 :t2}
          :logic-alias :a})

  (l/run* [q]
          (l/and*
           [(l/membero q [:t1 :t2 :t3])
            (l/membero q [:t2 :t3])
            (l/membero q [:t1 :t2])]))


  (l/run* [q]
          (->> [a b c]
               (map :tags)
               (map vec)
               (map (fn [xs]
                      xs
                      (l/membero q xs)))
               (l/and*)))

 ;;
  )

