(ns project.ext.base.proc
  (:require
   [project.core.protocols :as core.p]
   [project.ext.base.protocols :as base.p]
   [project.ext.render.api :as render.api]))


(defn create-proc-ext
  [channels]
  (go (loop []
        (let [v (<! (chan 1))])))
  (with-meta
    {}
    {`core.p/mount* (fn [_]
                      (core.p/mount* render.api/proc-ext))
     `core.p/unmount* (fn [_])}))

