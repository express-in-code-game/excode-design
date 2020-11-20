(ns deathstar.ui.spec
  #?(:cljs (:require-macros [deathstar.ui.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::url string?)

(s/def ::op-key {::request-response ::fire-and-forget ::request-stream ::request-channel})

(s/def ::host string?)
(s/def ::port int?)
(s/def ::path string?)

(s/def ::num-code int?)
(s/def ::reason-text string?)
(s/def ::error any?)
(s/def ::reconnection-timeout int?)