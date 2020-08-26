(ns deathstar.server.chan
  #?(:cljs (:require-macros [deathstar.server.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [cljctools.csp.op.spec :as op.spec]
   [deathstar.server.spec :as server.spec]))

(do (clojure.spec.alpha/check-asserts true))

(defmulti ^{:private true} op* op.spec/op-spec-dispatch-fn)
(s/def ::op (s/multi-spec op* op.spec/op-spec-retag-fn))
(defmulti op op.spec/op-dispatch-fn)

(defn create-channels
  []
  (let [ops| (chan 10)
        ops|m (mult ops|)]
    {::ops| ops|
     ::ops|m ops|m}))

(defmethod op*
  {::op.spec/op-key ::init} [_]
  (s/keys :req [::server.spec/options]
          :req-un []))

(defmethod op
  {::op.spec/op-key ::init}
  [op-meta channels options]
  (put! (::ops| channels) (merge
                           op-meta
                           {::server.spec/options options})))
