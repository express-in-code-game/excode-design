(ns starnet.common.alpha.user
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   ))


(defmulti next-state-user
  "Returns next state of the user record"
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-state-user [:ev.u/create]
  [state k ev]
  (or state ev))

(defmethod next-state-user [:ev.u/update]
  [state k ev]
  (when state
    (merge state ev)))

(defmethod next-state-user [:ev.u/delete]
  [state k ev]
  nil)

