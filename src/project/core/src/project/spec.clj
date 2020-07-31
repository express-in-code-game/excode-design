(ns project.spec
  (:require
   [clojure.spec.alpha :as s]))

(def ^:const OP :op)

(s/def ::op-key #{OP})
(s/def ::out| any?)



(def ops
  {:app.main/mount (s/keys :req [::op-key ::out|])})

(def channels
  {:main/ops| #{:app.main/mount}
   :game| #{}
   :render/ops| #{}})

(def ops-keys (keys ops))
(def channels-keys (keys channels))


;; (defmulti op-type OP)

;; (defmethod op-type :app.main/mount
;;   [vl]
;;   (s/keys :req [::op ::out|]))

;; (s/def ::op (s/multi-spec op-type OP))
;; 

(s/def ::channel-exists channels-keys)
(s/def ::op-exists #(ops-keys (OP %)))
(s/def ::channel-op-exists (fn [{:keys [channel-key val-map]}]
                             ((channel-key channels) (OP val-map))))
(s/def ::val-map-valid (fn [{:keys [channel-key val-map]}]
                         (s/valid? ((OP val-map) ops) val-map)))

(defmacro vl
  [channel-key val-map]
  `~m)

(s/fdef vl
  :args (s/and
         (s/cat :channel-key ::channel-exists
                :val-map ::op-exists)
         ::channel-op-exists
         ::val-map-valid)
  :ret any?)
