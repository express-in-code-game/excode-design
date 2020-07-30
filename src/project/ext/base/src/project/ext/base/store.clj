(ns project.ext.base.store
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [project.core.protocols :as core.p]
   [project.core]))


(defn make-default-state
  []
  {:app/title "App title"
   :ext/fx-content {}})

(def state* (atom (make-default-state)))

(defmulti write
  {:arglists '([vl])}
  (fn [vl] (:op vl)))

(defmethod write :app/title
  [{:keys [op data]}]
  (swap! state* assoc :app/title data))

(defmethod write :ext/fx-content-add
  [{:keys [op data]}]
  (swap! state* update :ext/fx-content assoc (:ext/key data) data))

(defn create-proc-store
  [channels]
  (let [ops| (chan 10)
        operation (project.core/operation-fn ops|)]
    (go (loop []
          (when-let [{:keys [op opts out|]} (<! ops|)]
            (condp = op
              :mount (let []
                       (prn :mount)
                       (put! out| 123)
                       (close! out|))
              :unmount (future (let []
                                 (prn :unmount)))))))
    (reify core.p/Mountable
      (core.p/mount* [_ opts] (operation :mount opts))
      (core.p/unmount* [_ opts] (operation :unmount opts)))

    #_(with-meta
        {}
        {'Mountable '_
         `core.p/mount* (fn [_])
         `core.p/unmount* (fn [_])})))

