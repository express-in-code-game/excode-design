(ns project.ext.server.render
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [cljfx.api :as fx]
   [project.core.protocols :as core.p]
   [project.core]
   [project.ext.base.store :as base.store])
  (:import
   (javafx.application Platform)))

(def inputs|* (chan (sliding-buffer 100)))

(defn input!
  [op data]
  (put! inputs|* {:op op :data data}))

(def flow-pane
  {:fx/type :flow-pane
   :vgap 5
   :hgap 5
   :padding 5
   :children (repeat 100 {:fx/type :rectangle :width 25 :height 25})})

(def content (let [ext-key :project.ext/server]
               {:ext/key ext-key
                :ext/fx-tab {:fx/type :tab
                             :fx/key ext-key
                             :text "server"
                             :closable false
                             :content flow-pane}}))

(defn create-proc-render
  [channels]
  (let [ops| (chan 10)
        inputs| inputs|*
        operation (project.core/operation-fn ops|)]
    (go (loop []
          (when-let [[vl port] (alts! [ops| inputs|])]
            (condp = port
              ops| (let [{:keys [op opts out|]} vl]
                     (condp = op
                       :mount (let []
                                (put! out| 123)
                                (close! out|))
                       :unmount (future (let []
                                          (prn :unmount)))))

              inputs| (let [{:keys [op data]} vl]
                        (condp = op
                          :some-input (let []
                                        (base.store/write {:op :some-input :data data}))))))
          (recur)))
    (reify core.p/Mountable
      (core.p/mount* [_ opts] (operation :mount opts))
      (core.p/unmount* [_ opts] (operation :unmount opts)))
    #_(with-meta
        {}
        {'Mountable '_
         `core.p/mount* (fn [_])
         `core.p/unmount* (fn [_])})))


#_(renderer)
#_(fx/unmount-renderer *state renderer)