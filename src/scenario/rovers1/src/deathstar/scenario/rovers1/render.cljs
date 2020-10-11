(ns deathstar.scenario.rovers1.render
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(defn create-state
  []
  (r/atom {}))

(defn rc-repl-tab
  [{:keys [ops|]} ctx]
  (r/with-let [state (ctx :state)
               pos (r/cursor state [:pos])]
    [:<>
     [:div {} (str @state)]]))

(defn- render-ui
  [channels ctx {:keys [id] :or {id "ui"}}]
  (rdom/render [rc-repl-tab channels ctx]  (.getElementById js/document id)))

(defn create-channels
  []
  (let [ops| (chan 10)
        input| (chan (sliding-buffer 10))
        input|m (mult input|)]
    {::ops| ops|
     ::input| input|
     ::input|m input|m}))

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::ops|]} channels
        {:keys [state]} ctx
        input|t (tap input|m (chan (sliding-buffer 10)))]
    (go
      (loop []
        (when-let [[v port] (alts! [ops| input|t])]
          (condp = port
            ops| (condp = (:op v)

                   ::render
                   (render-ui channels ctx {:id (:id v)}))
            input|t (condp = (:op v)

                      ::some-op
                      (do nil))))
        (recur)))))

(defn render
  [channels dom-element-id]
  (put! (::input| channels) {:op ::render :id dom-element-id}))

