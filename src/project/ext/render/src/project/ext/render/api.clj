(ns project.ext.render.api
  (:require
   [project.ext.render.proc :refer [create-proc-ext]]))

(def channels (let []
                {}))

(def proc-ext (create-proc-ext channels))

(defn bar [])