(ns cljctools.vscode.spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))


(do (clojure.spec.alpha/check-asserts true))

(s/def ::opkeys #{:vscode/mount :vscode/unmount})

(s/def ::chkey keyword?)

(s/def ::opkey ::opkeys)

(s/def ::args (s/cat :chkey ::chkey
                     :opkey ::opkey))

(defmacro assert-op
  [chkey opkey]
  `(do
     (s/assert ::chkey  ~chkey)
     (s/assert ::opkey  ~opkey)
     #_(when-not (opkeys ~opkey)
         (throw (Exception. "no such op")))))

(defmacro op2
  [chkey opkey]
  (assert-op chkey opkey)
  `~opkey)