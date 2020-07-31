(ns project.spec
  (:require
   [clojure.spec.alpha :as s]))

(def ^:const OP :op)

(s/def ::op-key #{OP})
(s/def ::out| any?)

(def vals
  {:app.main/mount (s/keys :req [::op-key ::out|])})

(defmulti op-type OP)

(defmethod op-type :app.main/mount
  [vl]
  (s/keys :req [::op ::out|]))

(s/def ::op (s/multi-spec op-type OP))