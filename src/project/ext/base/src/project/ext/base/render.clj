(ns project.ext.base.render
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [cljfx.api :as fx]
   [project.core.protocols :as core.p]
   [project.core]
   [project.ext.base.protocols :as render.p])
  (:import
   (javafx.application Platform)))

(def *state
  (atom {:title "App title"}))

(defn title-input [{:keys [title]}]
  {:fx/type :text-field
   :on-text-changed #(swap! *state assoc :title %)
   :text title})

(def title-demo
  {:fx/type :v-box
   :children [{:fx/type :label
               :text "Window title input"}
              {:fx/type title-input
               :title "asd"}]})

#_(System/getProperty "java.version")
#_(com.sun.javafx.runtime.VersionInfo/getRuntimeVersion)

(def v-box
  {:fx/type :v-box
   :spacing 5
   :fill-width true
   :alignment :top-center
   :children [{:fx/type :label :text "just label"}
              {:fx/type :label
               :v-box/vgrow :always
               :style {:-fx-background-color :lightgray}
               :max-height Double/MAX_VALUE
               :max-width Double/MAX_VALUE
               :text "expanded label"}]})

(def tabs
  {:fx/type :tab-pane
   :pref-width 1600
   :pref-height 900
  ;;  :percent-width 80
  ;;  :percent-height 80
   :tabs [{:fx/type :tab
           :text "settings"
           :closable false
           :content v-box}
          {:fx/type :tab
           :text "scenarios"
           :closable false
           :content v-box}
          {:fx/type :tab
           :text "game1s"
           :closable false
           :content v-box}
          {:fx/type :tab
           :text "connect"
           :closable false
           :content v-box}
          {:fx/type :tab
           :text "server"
           :closable false
           :content title-demo}]})


(defn root [{:keys [title]}]
  {:fx/type :stage
   :showing true
   :title title
   :always-on-top true
   :scene {:fx/type :scene
           :root tabs}})

(def renderer
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)))

(defn mount-fx []
  (Platform/setImplicitExit true)
  (fx/mount-renderer *state renderer))

(defn create-proc-render
  [channels]
  (let [ops| (chan 10)
        operation (project.core/operation-fn ops|)]
    (go (loop []
          (when-let [{:keys [op opts out|]} (<! ops|)]
            (condp = op
              :mount (let []
                       (prn :mount)
                       (mount-fx)
                       (<! (timeout 1000))
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


#_(renderer)
#_(fx/unmount-renderer *state renderer)