(ns project.ext.base.main
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [project.core.protocols :as core.p]
   [project.ext.base.protocols :as base.p]
   [project.ext.base.render :as base.render]
   [project.ext.base.store :as base.store]))


(def channels (let []
                {}))

(defn create-proc-main
  [{:keys [] :as channels} {:keys [proc-render proc-store]}]
  (go (loop []
        (let [v (<! (chan 1))])))
  (reify
    core.p/Mountable
    (core.p/mount* [_]
      (core.p/mount* proc-render)
      (core.p/mount* proc-store))
    (core.p/unmount* [_]))
  #_(with-meta
      {}
      {`core.p/mount* (fn [_])
       `core.p/unmount* (fn [_])}))


(def proc-render (base.render/create-proc-render channels))

(def proc-store (base.store/create-proc-store channels))

(def proc-main (create-proc-main channels {:proc-render proc-render
                                           :proc-store proc-store}))

(defn mount []
  (core.p/mount* proc-main))



