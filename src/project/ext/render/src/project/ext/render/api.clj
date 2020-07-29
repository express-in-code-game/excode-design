(ns project.ext.render.api
  (:require [project.ext.connect.impl.proc :refer [create-proc-ext]]))

(def channels (let []
                {}))

(def proc-ext (create-proc-ext channels))

(defn foo [])

(defn bar [])