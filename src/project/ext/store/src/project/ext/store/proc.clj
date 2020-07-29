(ns project.ext.store.proc
  (:require
   [project.core.protocols :as core.p]
   [project.ext.store.protocols :as store.p]))


(defn create-proc-ext
  [channels]
  (go (loop []
        (let [v (<! (chan 1))])))
  (with-meta
    {}
    {`core.p/mount* (fn [_])
     `core.p/unmount* (fn [_])}))

