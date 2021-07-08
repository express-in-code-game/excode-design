(ns deathstar.scenario-api.spec
  #?(:cljs (:require-macros [deathstar.scenario-api.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::foo keyword?)
