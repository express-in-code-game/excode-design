(ns project.spec
  (:require
   [clojure.spec.alpha :as s]))

(def ^:const OP :op)

(s/def ::op-key #{OP})
(s/def ::out| any?)

(def channel-keys #{:main/ops|
                    :game|
                    :render/ops|})

(def ops
  {:app.main/mount (s/keys :req [::op-key ::out|])})

(def op-keys (set (keys ops)))

;; (defmulti op-type OP)

;; (defmethod op-type :app.main/mount
;;   [vl]
;;   (s/keys :req [::op ::out|]))

;; (s/def ::op (s/multi-spec op-type OP))
;; 

(defmacro vl
  [channel-key val-map]
  `~m)

(s/fdef vl
  :args (s/and
         (s/cat :channel-key channel-keys
                :val-map #(ops (:op %)))
         (fn [{:keys [channel-key val-map] :as argm}]
           
           ))
  :ret any?)
