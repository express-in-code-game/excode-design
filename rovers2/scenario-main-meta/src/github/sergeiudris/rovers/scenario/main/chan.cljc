(ns github.sergeiudris.rovers.scenario.main.chan
  #?(:cljs (:require-macros [github.sergeiudris.rovers.scenario.main.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [cljctools.csp.op.spec :as op.spec]
   [github.sergeiudris.rovers.scenario.main.spec :as scenario.main.spec]))

(do (clojure.spec.alpha/check-asserts true))

(defmulti ^{:private true} op* op.spec/op-spec-dispatch-fn)
(s/def ::op (s/multi-spec op* op.spec/op-spec-retag-fn))
(defmulti op op.spec/op-dispatch-fn)


(defn create-channels
  []
  (let [ops| (chan 10)]
    {::ops| ops|}))

(defmethod op*
  {::op.spec/op-key ::init} [_]
  (s/keys :req []))

(defmethod op
  {::op.spec/op-key ::init}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))