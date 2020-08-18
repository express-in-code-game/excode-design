(ns deathstar.tabapp.solution-space.render
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(defn create-state
  []
  (r/atom {:data []}))

(defn rc-repl-tab
  [{:keys [ops|]} ctx]
  (r/with-let [data (r/cursor (ctx :state) [:data])]
    [:<>
     #_[:div {} "rc-repl-tab"]
     #_[:button {:on-click (fn [e]
                             (println "button clicked")
                             #_(put! ops| ???))} "button"]
     #_[:div ":conf"]
     #_[:div {} (with-out-str (pprint @conf))]
     #_[:div @lrepl-id]
     #_[:div @ns-sym]
     [:br]
     [:div ":data"]
     [:section
      (map-indexed (fn [i v]
                     ^{:key i} [:pre {} (with-out-str (pprint v))])
                   @data)]]))

(defn render-ui
  [channels ctx]
  (rdom/render [rc-repl-tab channels ctx]  (.getElementById js/document "ui")))