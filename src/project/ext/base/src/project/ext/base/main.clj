(ns project.ext.base.main
  (:require
   [project.core.protocols :as core.p]
   [project.ext.base.protocols :as base.p]
   [project.ext.base.render :as base.render]
   [project.ext.base.store :as base.store]))


(def channels (let []
                {}))

(defn create-proc-main
  [channels {:keys [proc-render]}]
  (go (loop []
        (let [v (<! (chan 1))])))
  (with-meta
    {}
    {`core.p/mount* (fn [_]
                      (base.render/mount proc-render)
                      (base.render/unmount store.api/proc-ext))
     `core.p/unmount* (fn [_])}))


(def proc-render (base.render/create-proc-render channels))

(def proc-main (create-proc-main channels {:proc-render proc-render}))



