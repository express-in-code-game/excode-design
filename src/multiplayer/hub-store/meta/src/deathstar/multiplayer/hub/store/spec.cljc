(ns deathstar.multiplayer.hub.store.spec
  #?(:cljs (:require-macros [deathstar.multiplayer.hub.spec]))
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))

(do (clojure.spec.alpha/check-asserts true))

(def op-specs
  {::run-dev-simulation (s/keys :req-un [::op])
   ::run-release-simulation (s/keys :req-un [::op])
   ::reset-dev-simulation (s/keys :req-un [::op])})

(def ch-specs
  {::gamestate| #{}})

(def op-keys (set (keys op-specs)))
(def ch-keys (set (keys ch-specs)))

(s/def ::op op-keys)

(s/def ::ch-exists ch-keys)
(s/def ::op-exists (fn [v] (op-keys (if (keyword? v) v (:op v)))))
(s/def ::ch-op-exists (s/cat :ch ::ch-exists :op ::op-exists))

(defmacro op
  [chkey opkey]
  (s/assert ::ch-exists  chkey)
  (s/assert ::op-exists  opkey)
  #_(s/assert ::ch-op-exists  opkey)
  `~opkey)

(defmacro vl
  [chkey v]
  (s/assert ::ch-exists  chkey)
  (s/assert ::op-exists  v)
  `~v)
