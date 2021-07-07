(ns project.ext.scenarios.main
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [project.core.protocols :as core.p]
   [project.core]
   [project.ext.scenarios.render :as scenarios.render]
   [project.ext.base.store :as base.store]))


(def channels (let []
                {}))


(defn create-proc-main
  [{:keys [] :as channels} {:keys [proc-render]}]
  (let [ops| (chan 10)
        operation (project.core/operation-fn ops|)]
    (go (loop []
          (when-let [{:keys [op opts out|]} (<! ops|)]
            (condp = op
              :mount (let []
                       (base.store/tx-fx-content-add scenarios.render/content)
                       (<! (core.p/mount* proc-render {}))
                       (put! out| true)
                       (close! out|))
              :unmount (future (let []
                                 (prn :unmount)))))))
    (reify
      core.p/Mountable
      (core.p/mount* [_ opts] (operation :mount opts))
      (core.p/unmount* [_ opts] (operation :unmount opts)))
    #_(with-meta
        {}
        {`core.p/mount* (fn [_])
         `core.p/unmount* (fn [_])})))


(def proc-render (scenarios.render/create-proc-render channels))

(def proc-main (create-proc-main channels {:proc-render proc-render}))

(defn mount []
  (core.p/mount* proc-main {}))



