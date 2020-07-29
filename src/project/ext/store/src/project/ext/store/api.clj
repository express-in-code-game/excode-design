(ns project.ext.store.api
  (:require [project.ext.store.proc :refer [create-proc-ext]]))

(def channels (let []
                {}))

(def proc-ext (create-proc-ext channels))

(defn foo [])

(defn bar [])