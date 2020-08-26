(ns deathstar.server.spec
  #?(:cljs (:require-macros [deathstar.server.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::options some?)