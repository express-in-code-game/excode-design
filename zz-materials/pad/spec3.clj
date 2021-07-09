(ns pad.spec1
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [clojure.spec.alpha :as s]))

(defmacro tmp1
  [k m]
  `~m)

(s/fdef clojure.core/declare
  :args (s/cat :names (s/* simple-symbol?))
  :ret any?)


(def channel-keys #{:a| :b| :c|})
(def ops #{:o1 :o2 :o3})
(def channel-ops {:a| #{:o1 :o2}
                  :b| #{:o3}})
(def val-maps {:o1 #{:op :data}
               :o3 #{:op :data :out|}})

#_(s/fdef tmp1
    :args (s/cat :channel-key (s/and  #_any? channel-keys)
                 :val-map (s/and (fn [v] (and
                                          (ops (:op v))))))
    :ret any?)

(s/fdef tmp1
  :args (s/and
         (s/cat :channel-key channel-keys
                :vals-map #(ops (:op %)))
         (fn [{:keys [channel-key vals-map] :as argm}]
           (prn argm)
           (and
            ((channel-ops channel-key) (:op vals-map))
            (clojure.set/subset? (val-maps (:op vals-map)) (set (keys vals-map))))))
  :ret any?)

(defn tmp2 []
  (tmp1 :a| {:op :o1 :dat1a 3})
  #_(declare ""))

(comment

  (tmp1 :a| {:op :o1 :dat1a 3})

  (contains?)

  ;;
  )