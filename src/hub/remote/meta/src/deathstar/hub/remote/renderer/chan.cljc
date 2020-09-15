(ns deathstar.extension.gui.chan
  #?(:cljs (:require-macros [deathstar.extension.gui.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [deathstar.extension.spec :as extension.spec]
   [cljctools.csp.op.spec :as op.spec]))

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
  (s/keys :req []
          :req-un []))

(defmethod op
  {::op.spec/op-key ::init}
  ([op-meta channels]
   (op op-meta channels (chan 1)))
  ([op-meta channels out|]
   (put! (::ops| channels)
         (merge op-meta
                {}))
   out|))

(defmethod op*
  {::op.spec/op-key ::update-state} [_]
  (s/keys :req [::extension.spec/state]
          :req-un []))

(defmethod op
  {::op.spec/op-key ::update-state}
  ([op-meta channels state]
   (op op-meta channels state nil))
  ([op-meta channels state extra]
   (put! (::ops| channels)
         (merge op-meta
                extra
                {::extension.spec/state state}))))