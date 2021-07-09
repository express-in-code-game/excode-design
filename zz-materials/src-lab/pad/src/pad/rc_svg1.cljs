(ns pad.rc-svg1
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(defn rc-svg-defs-grid
  [channels state]
  (reagent.core/with-let []
    [:div {:style {:width "400px" :height "300px"}}
     [:svg {:width "100%" :height "100%" :xmlns "http://www.w3.org/2000/svg"}
      [:defs
       [:pattern {:id "smallGrid" :width 8 :height 8
                  :patternUnits "userSpaceOnUse"}
        [:path {:d "M 8 0 L 0 0 0 8"
                :fill "none"
                :stroke "grey"
                :stroke-width "0.5"}]]
       [:pattern {:id "largeGrid" :width 80 :height 80
                  :patternUnits "userSpaceOnUse"}
        [:rect {:width 80
                :height 80
                :fill "url(#smallGrid)"}]
        [:path {:d "M 80 0 L 0 0 0 80"
                :fill "none"
                :stroke "grey"
                :stroke-width "1"}]]]
      [:rect {:width "100%" :height "100%" :fill "url(#largeGrid)"}]]]))