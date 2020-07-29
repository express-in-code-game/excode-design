(ns project.ext.base.main
  (:require
   [project.core.protocols :as core.p]
   [project.ext.base.protocols :as base.p]
   [project.ext.base.render :as render]
   [project.ext.store.api :as store.api]))


(def channels (let []
                {}))

(defn create-proc
  [channels]
  (go (loop []
        (let [v (<! (chan 1))])))
  (with-meta
    {}
    {`core.p/mount* (fn [_]
                      (core.p/mount* proc-render)
                      (core.p/mount* store.api/proc-ext))
     `core.p/unmount* (fn [_])}))

(def proc-ext (create-proc-ext channels))



