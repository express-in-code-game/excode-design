(ns project.ext.base.api
  (:require [project.ext.base.impl.proc :refer [create-proc-ext]])
  )

(def channels (let []
                {}))

(def proc-ext (create-proc-ext channels))

(defn foo [])

(defn bar [])