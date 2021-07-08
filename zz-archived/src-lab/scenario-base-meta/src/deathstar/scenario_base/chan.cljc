(ns deathstar.scenario-base.chan
  #?(:cljs (:require-macros [deathstar.scenario-base.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [cljctools.csp.op.spec :as op.spec]
   [deathstar.scenario-base.spec :as scenario-base.spec]))

(do (clojure.spec.alpha/check-asserts true))

(defmulti ^{:private true} op* op.spec/op-spec-dispatch-fn)
(s/def ::op (s/multi-spec op* op.spec/op-spec-retag-fn))
(defmulti op op.spec/op-dispatch-fn)


(defn create-channels
  []
  (let [ops| (chan 10)
        release| (chan 1)]
    {::ops| ops|
     ::release| release|}))

(defmethod op*
  {::op.spec/op-key ::init
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req []))
(derive ::init ::op)
(defmethod op
  {::op.spec/op-key ::init
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))