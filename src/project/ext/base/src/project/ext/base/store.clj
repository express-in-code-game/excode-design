(ns project.ext.base.store
  (:require [project.core.protocols :as core.p]))


(def store (atom {}))


(defn create-proc-store
  [channels]
  (let []
    (with-meta
      {}
      {'Mountable '_
       `core.p/mount* (fn [_])
       `core.p/unmount* (fn [_])})))

