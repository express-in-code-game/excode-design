(ns deathstar.peernode.chan
  #?(:cljs (:require-macros [deathstar.peernode.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [cljctools.csp.op.spec :as op.spec]
   [deathstar.peernode.spec :as peernode.spec]))

(do (clojure.spec.alpha/check-asserts true))

(defmulti ^{:private true} op* op.spec/op-spec-dispatch-fn)
(s/def ::op (s/multi-spec op* op.spec/op-spec-retag-fn))
(defmulti op op.spec/op-dispatch-fn)


(defn create-channels
  []
  (let [ops| (chan 10)]
    {::ops| ops|}))

(defmethod op*
  {::op.spec/op-key ::id
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req []))

(defmethod op
  {::op.spec/op-key ::id
   ::op.spec/op-type ::op.spec/request}
  [op-meta channels]
  (put! (::ops| channels) (merge op-meta)))


(defmethod op*
  {::op.spec/op-key ::id
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req [::peernode.spec/id]))

(defmethod op
  {::op.spec/op-key ::id
   ::op.spec/op-type ::op.spec/response}
  [op-meta channels value]
  (put! (::ops| channels) value))
