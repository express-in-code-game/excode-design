(ns deathstar.scenario-base.spec
  #?(:cljs (:require-macros [deathstar.scenario-base.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::foo keyword?)
