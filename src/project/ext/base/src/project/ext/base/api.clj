(ns project.ext.base.api
  (:require
   [project.core.protocols :as core.p]))


(defn mount [proc]
  (core.p/mount* proc))

(defn bar [])