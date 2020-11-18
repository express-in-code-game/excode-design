(ns deathstar.peernode.spec
  #?(:cljs (:require-macros [deathstar.peernode.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::id string?)
