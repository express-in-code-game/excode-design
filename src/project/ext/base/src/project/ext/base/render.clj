(ns project.ext.base.render
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [cljfx.api :as fx]
   [clojure.core.cache :as cache]
   [project.core.protocols :as core.p]
   [project.core]
   [project.ext.base.store :as base.store]
   [project.ext.base.protocols :as render.p])
  (:import
   (javafx.application Platform)))

(def state* (atom (fx/create-context @base.store/state* cache/lru-cache-factory)))

(do (add-watch base.store/state* :watcher
               (fn [key ref v-old v-new]
                 (reset! state* (fx/reset-context @state* v-new)))))

(def inputs|* (chan (sliding-buffer 100)))

(defn input!
  [op data]
  (put! inputs|* {:op op :data data}))

(defn title-input [{:keys [title]}]
  {:fx/type :text-field
   :on-text-changed (fn [x]
                      (input! :app/title x))  #_#(swap! *state assoc :title %)
   :text title})

(def title-demo
  {:fx/type :v-box
   :children [{:fx/type :label
               :text "Window title input"}
              {:fx/type title-input
               :title "abc"}]})

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

(defn tabs [{:keys [fx/context]}]
  (let [exts (fx/sub context :ext/fx-content)]
    (println exts)
    (println "tabs")
    (println (mapv (fn [[k v]] ((:ext/fx-tab-fn v))) exts))
    {:fx/type :tab-pane
     :pref-width 1600
     :pref-height 900
     :tabs (into []
                 (comp
                  (filter (fn [[k v]] (#{:project.ext/scenarios} k)))
                  (map (fn [[k v]] ((:ext/fx-tab-fn v))))
                  exts))
     #_[{:fx/type :tab
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
  )


(defn root [{:keys [fx/context]}]
  {:fx/type :stage
   :showing true
   :title (fx/sub context :app/title)
   :always-on-top true
   :scene {:fx/type :scene
           :root {:fx/type :scroll-pane
                  :fit-to-width true
                  :fit-to-height true
                  :content {:fx/type tabs}}}})


(def renderer
  (fx/create-renderer
   :middleware (comp
                fx/wrap-context-desc
                (fx/wrap-map-desc (fn [_] {:fx/type root})))
   :opts {:fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                       (fx/fn->lifecycle-with-context %))}))

(defn mount-fx []
  (Platform/setImplicitExit true)
  (fx/mount-renderer state* renderer))

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
                                (prn :mount)
                                (mount-fx)
                                (put! out| true)
                                (close! out|))
                       :unmount (future (let []
                                          (prn :unmount)))))

              inputs| (let [{:keys [op data]} vl]
                        (condp = op
                          :app/title (let []
                                       (base.store/write {:op :app/title :data data}))))))
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