(ns project.ext.base.api
  (:require 
   [project.core.protocols :as core.p]
   [project.ext.base.proc :refer [create-proc-ext]]))

(def channels (let []
                {}))

(def proc-ext (create-proc-ext channels))

(defn mount []
  (core.p/mount* proc-ext))

(defn bar [])