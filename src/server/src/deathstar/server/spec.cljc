(ns deathstar.server.spec
  #?(:cljs (:require-macros [deathstar.worker.spec]))
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))

(do (clojure.spec.alpha/check-asserts true))

(def ^:const OP :op)
(s/def ::out| any?)

(def op-specs
  {:hello (s/keys :req-un [::op #_::out|])})

(def ch-specs
  {:some| #{:hello}})

(def op-keys (set (keys op-specs)))
(def ch-keys (set (keys ch-specs)))


(s/def ::op op-keys)

(s/def ::ch-exists ch-keys)
(s/def ::op-exists (fn [v] (op-keys (if (keyword? v) v (OP v)))))
(s/def ::ch-op-exists (s/cat :ch ::ch-exists :op ::op-exists))

(defmacro op
  [chkey opkey]
  (s/assert ::ch-exists  chkey)
  (s/assert ::op-exists  opkey)
  `~opkey)

(defmacro vl
  [chkey v]
  (s/assert ::ch-exists  chkey)
  `~v)
