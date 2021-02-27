(ns deathstar.data.spec
  #?(:cljs (:require-macros [deathstar.data.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::foo keyword?)
(s/def ::bar keyword?)