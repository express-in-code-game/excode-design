(ns project.spec
  (:require
   [clojure.spec.alpha :as s]))

(def ^:const OP :op)

(s/def ::op #{OP})
(s/def ::out| any?)

(def ops
  {:project.app.main/mount (s/keys :req-un [::op #_::out|])})

(def channels
  {:main/ops| #{:project.app.main/mount}
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
(s/def ::op-exists #(ops-keys (if (keyword? %) % (OP %))))
(s/def ::channel-op-exists (fn [{:keys [channel-key val-map op-key]}]
                             ((channel-key channels) (or op-key (OP val-map)))))
(s/def ::val-map-valid (fn [{:keys [channel-key val-map]}]
                         (s/valid? ((OP val-map) ops) val-map)))

(defmacro op
  [channel-key op-key]
  `~op-key)

(s/fdef op
  :args (s/and
         (s/cat :channel-key ::channel-exists
                :op-key ::op-exists)
         ::channel-op-exists)
  :ret any?)

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
