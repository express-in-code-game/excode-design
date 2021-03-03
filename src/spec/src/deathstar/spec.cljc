(ns deathstar.spec
  #?(:cljs (:require-macros [deathstar.spec]))
  (:require
   [clojure.spec.alpha :as s]))


(s/def ::username string?)
(s/def ::password string?)

(s/def ::user-info (s/keys :req [::username]))