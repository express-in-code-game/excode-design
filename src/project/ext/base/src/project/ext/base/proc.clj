(ns project.ext.base.proc
  (:require
   [project.ext.base.meta.protocols :as base.p]
   [project.ext.render.proc.main :as render.main]
   [project.ext.scenarios.proc.main :as scenarios.main]
   [project.ext.console.proc.main :as console.main]))


(defn create-proc-ext
  [channels]
  (go (loop []
        (let [v (<! (chan 1))])))
  (with-meta
    {}
    {`mount (fn [_]
              (render.main/-main))
     `unmount (fn [_])}))

