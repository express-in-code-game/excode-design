(ns project.ext.games.render
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

(def grid-pane
  {:fx/type :grid-pane
   :children (concat
              (for [i (range 16)]
                {:fx/type :label
                 :grid-pane/column i
                 :grid-pane/row i
                 :grid-pane/hgrow :always
                 :grid-pane/vgrow :always
                 :text "boop"})
              [{:fx/type :label
                :grid-pane/row 2
                :grid-pane/column 3
                :grid-pane/column-span 2
                :text "I am a long label spanning 2 columns"}])})

(def content (let [ext-key :project.ext/games]
               {:ext/key ext-key
                :ext/fx-tab-fn (fn []
                                 {:fx/type :tab
                                  :fx/key ext-key
                                  :text "games"
                                  :closable false
                                  :content grid-pane})}))

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
                          :some-input (let [])))))
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