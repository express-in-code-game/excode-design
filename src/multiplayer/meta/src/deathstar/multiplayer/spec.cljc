(ns deathstar.multiplayer.spec
  #?(:cljs (:require-macros [deathstar.multiplayer.spec]))
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))


(s/def ::uuid uuid?)
(s/def ::username string?)

(s/def ::user (s/keys ::req [::uuid ::username]))

