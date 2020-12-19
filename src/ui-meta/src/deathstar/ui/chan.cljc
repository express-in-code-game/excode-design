(ns deathstar.ui.chan
  #?(:cljs (:require-macros [deathstar.ui.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [cljctools.csp.op.spec :as op.spec]
   [deathstar.ui.spec :as ui.spec]))

(do (clojure.spec.alpha/check-asserts true))

(defmulti ^{:private true} op* op.spec/op-spec-dispatch-fn)
(s/def ::op (s/multi-spec op* op.spec/op-spec-retag-fn))
(defmulti op op.spec/op-dispatch-fn)


(defn create-channels
  []
  (let [ops| (chan 10)]
    {::ops| ops|}))

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


(defmethod op*
  {::op.spec/op-key ::update-state
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req []))
(defmethod op
  {::op.spec/op-key ::update-state
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))


(defmethod op*
  {::op.spec/op-key ::mount-tournament
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request} [_]
  (s/keys :req []))
(derive ::mount-tournament ::op)
(defmethod op
  {::op.spec/op-key ::mount-tournament
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request}
  ([op-meta channels value]
   (op op-meta channels value (chan 1)))
  ([op-meta channels value out|]
   (put! (::ops| channels) (merge op-meta
                                  value
                                  {::op.spec/out| out|}))
   out|))
(defmethod op*
  {::op.spec/op-key ::mount-tournament
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response} [_]
  (s/keys :req []))
(derive ::mount-tournament ::op)
(defmethod op
  {::op.spec/op-key ::mount-tournament
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response}
  [op-meta out| value]
  (put! out| (merge op-meta
                    value)))


(defmethod op*
  {::op.spec/op-key ::unmount-tournament
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request} [_]
  (s/keys :req []))
(derive ::unmount-tournament ::op)
(defmethod op
  {::op.spec/op-key ::unmount-tournament
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request}
  ([op-meta channels value]
   (op op-meta channels value (chan 1)))
  ([op-meta channels value out|]
   (put! (::ops| channels) (merge op-meta
                                  value
                                  {::op.spec/out| out|}))
   out|))
(defmethod op*
  {::op.spec/op-key ::unmount-tournament
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response} [_]
  (s/keys :req []))
(derive ::unmount-tournament ::op)
(defmethod op
  {::op.spec/op-key ::unmount-tournament
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response}
  [op-meta out| value]
  (put! out| (merge op-meta
                    value)))


(defmethod op*
  {::op.spec/op-key ::mount-game
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request} [_]
  (s/keys :req []))
(derive ::mount-game ::op)
(defmethod op
  {::op.spec/op-key ::mount-game
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request}
  ([op-meta channels value]
   (op op-meta channels value (chan 1)))
  ([op-meta channels value out|]
   (put! (::ops| channels) (merge op-meta
                                  value
                                  {::op.spec/out| out|}))
   out|))
(defmethod op*
  {::op.spec/op-key ::mount-game
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response} [_]
  (s/keys :req []))
(derive ::mount-game ::op)
(defmethod op
  {::op.spec/op-key ::mount-game
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response}
  [op-meta out| value]
  (put! out| (merge op-meta
                    value)))


(defmethod op*
  {::op.spec/op-key ::unmount-game
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request} [_]
  (s/keys :req []))
(derive ::unmount-game ::op)
(defmethod op
  {::op.spec/op-key ::unmount-game
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request}
  ([op-meta channels value]
   (op op-meta channels value (chan 1)))
  ([op-meta channels value out|]
   (put! (::ops| channels) (merge op-meta
                                  value
                                  {::op.spec/out| out|}))
   out|))
(defmethod op*
  {::op.spec/op-key ::unmount-game
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response} [_]
  (s/keys :req []))
(derive ::unmount-game ::op)
(defmethod op
  {::op.spec/op-key ::unmount-game
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response}
  [op-meta out| value]
  (put! out| (merge op-meta
                    value)))


(defmethod op*
  {::op.spec/op-key ::mount-scenario
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request} [_]
  (s/keys :req []))
(derive ::mount-scenario ::op)
(defmethod op
  {::op.spec/op-key ::mount-scenario
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request}
  ([op-meta channels value]
   (op op-meta channels value (chan 1)))
  ([op-meta channels value out|]
   (put! (::ops| channels) (merge op-meta
                                  value
                                  {::op.spec/out| out|}))
   out|))
(defmethod op*
  {::op.spec/op-key ::mount-scenario
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response} [_]
  (s/keys :req []))
(derive ::mount-scenario ::op)
(defmethod op
  {::op.spec/op-key ::mount-scenario
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response}
  [op-meta out| value]
  (put! out| (merge op-meta
                    value)))


(defmethod op*
  {::op.spec/op-key ::unmount-scenario
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request} [_]
  (s/keys :req []))
(derive ::unmount-scenario ::op)
(defmethod op
  {::op.spec/op-key ::unmount-scenario
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request}
  ([op-meta channels value]
   (op op-meta channels value (chan 1)))
  ([op-meta channels value out|]
   (put! (::ops| channels) (merge op-meta
                                  value
                                  {::op.spec/out| out|}))
   out|))
(defmethod op*
  {::op.spec/op-key ::unmount-scenario
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response} [_]
  (s/keys :req []))
(derive ::unmount-scenario ::op)
(defmethod op
  {::op.spec/op-key ::unmount-scenario
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response}
  [op-meta out| value]
  (put! out| (merge op-meta
                    value)))