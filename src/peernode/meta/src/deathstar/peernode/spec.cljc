(ns deathstar.peernode.spec
  #?(:cljs (:require-macros [deathstar.peernode.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::id string?)

(s/def ::from ::id)
(s/def ::seqno string?)
(s/def ::data any?)
(s/def ::topic-ids (s/coll-of string?))
