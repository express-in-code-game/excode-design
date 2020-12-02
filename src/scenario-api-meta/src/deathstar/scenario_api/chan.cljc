(ns deathstar.scenario-api.chan
  #?(:cljs (:require-macros [deathstar.scenario-api.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [cljctools.csp.op.spec :as op.spec]
   [deathstar.scenario-api.spec :as scenario-api.spec]))

(do (clojure.spec.alpha/check-asserts true))

(defmulti ^{:private true} op* op.spec/op-spec-dispatch-fn)
(s/def ::op (s/multi-spec op* op.spec/op-spec-retag-fn))
(defmulti op op.spec/op-dispatch-fn)


(defn create-channels
  []
  (let [ops| (chan 10)]
    {::ops| ops|}))

(comment

  (derive ::request-response ::op-key)
  (derive ::fire-and-forget ::op-key)
  (derive ::request-stream ::op-key)
  (derive ::request-channel ::op-key)


  (isa? ::request-response ::op-key))


(defmethod op*
  {::op.spec/op-key ::update-state
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req []))
(derive ::update-state ::op)
#_(isa? ::update-state ::op)

(defmethod op
  {::op.spec/op-key ::update-state
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))

(defmethod op*
  {::op.spec/op-key ::generate
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req []))
(derive ::generate ::op)

(defmethod op
  {::op.spec/op-key ::generate
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))

(defmethod op*
  {::op.spec/op-key ::reset
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req []))
(derive ::reset ::op)

(defmethod op
  {::op.spec/op-key ::reset
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))


(defmethod op*
  {::op.spec/op-key ::resume
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req []))
(derive ::resume ::op)

(defmethod op
  {::op.spec/op-key ::resume
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))


(defmethod op*
  {::op.spec/op-key ::pause
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req []))
(derive ::pause ::op)

(defmethod op
  {::op.spec/op-key ::pause
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))

(defmethod op*
  {::op.spec/op-key ::next-step
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req []))
(derive ::next-step ::op)

(defmethod op
  {::op.spec/op-key ::next-step
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))