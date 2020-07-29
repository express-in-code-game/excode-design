(ns project.ext.base.proc
  (:require
   [project.core.protocols :as core.p]
   [project.ext.base.protocols :as base.p]
   [project.ext.base.proc.render :as proc.render]
   [project.ext.store.api :as store.api]))


(defn create-proc
  [channels]
  (go (loop []
        (let [v (<! (chan 1))])))
  (with-meta
    {}
    {`core.p/mount* (fn [_]
                      (core.p/mount* proc.render/proc-ext)
                      (core.p/mount* store.api/proc-ext))
     `core.p/unmount* (fn [_])}))

