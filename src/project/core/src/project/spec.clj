(ns project.spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))

(def ^:const OP :op)


(s/def ::out| any?)

(def ops
  {:project.app.main/mount (s/keys :req-un [::op #_::out|])})
(def ops-keys (set (keys ops)))
(s/def ::op ops-keys)

(def channels
  {:main/ops| #{:project.app.main/mount}
   :game| #{}
   :render/ops| #{}})


(def channels-keys (set (keys channels)))

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

#_(s/valid? (:project.app.main/mount ops) {:op :project.app.main/mount})
#_(gen/generate (s/gen (:project.app.main/mount ops)))

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
  `~val-map)

(s/fdef vl
  :args (s/and
         (s/cat :channel-key ::channel-exists
                :val-map ::op-exists)
         ::channel-op-exists
         ::val-map-valid)
  :ret any?)
