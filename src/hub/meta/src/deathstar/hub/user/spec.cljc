(ns deathstar.user.spec
  #?(:cljs (:require-macros [deathstar.user.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(do (clojure.spec.alpha/check-asserts true))

(s/def ::uuid uuid?)
(s/def ::username string?)

(s/def ::user (s/keys ::req [::uuid ::username]))

(s/def ::users (s/map-of keyword? ::user))