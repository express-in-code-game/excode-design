(ns project.ext.base.main
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [project.core.protocols :as core.p]
   [project.core]
   [project.ext.base.protocols :as base.p]
   [project.ext.base.render :as base.render]
   [project.ext.base.store :as base.store]))


(def channels (let []
                {}))

(defn create-proc-main
  [{:keys [] :as channels} {:keys [proc-render proc-store]}]
  (let [ops| (chan 10)
        operation (project.core/operation-fn ops|)]
    (go (loop []
          (when-let [{:keys [op opts out|]} (<! ops|)]
            (condp = op
              :mount (let []
                       (prn :mount)
                       (<! (core.p/mount* proc-render {}))
                       (<! (core.p/mount* proc-store {}))
                       (put! out| 123)
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


(def proc-render (base.render/create-proc-render channels))

(def proc-store (base.store/create-proc-store channels))

(def proc-main (create-proc-main channels {:proc-render proc-render
                                           :proc-store proc-store}))

(defn mount []
  (core.p/mount* proc-main {}))


(defprotocol P1
  (foo [_])
  (bar [_]))

(def p1 (reify P1
          (foo [_] 3)))

(defn x [p]
  (foo p)
  (bar p))



