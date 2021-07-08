(ns pad.konva1
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
   [reagent.dom :as rdom]

   ["react-konva" :rename {Stage KonvaStage
                           Layer KonvaLayer
                           Rect KonvaRect
                           Circle KonvaCircle}]))

(def konva-stage (reagent.core/adapt-react-class KonvaStage))
(def konva-layer (reagent.core/adapt-react-class KonvaLayer))
(def konva-rect (reagent.core/adapt-react-class KonvaRect))
(def konva-circle (reagent.core/adapt-react-class KonvaCircle))

(defn rc-konva-example-circle
  [channels state]
  (reagent.core/with-let []
    [konva-stage
     {:width js/window.innerWidth
      :height js/window.innerHeight}
     [konva-layer
      [konva-rect {:width 50
                   :height 50
                   :fill "red"}]
      [konva-circle {:width 200
                     :height 200
                     :stroke "black"
                     :radius 50
                     :fill "red"}]]]))

(defn rc-konva-grid
  [channels state]
  (reagent.core/with-let [width js/window.innerWidth
               height js/window.innerHeight
               box-size 15
               on-mouse-over (fn [evt]
                               (let [box (.-target evt)]
                                 (.fill box "#E5FF80")
                                 (.draw box)))
               on-mouse-out (fn [evt]
                              (let [box (.-target evt)]
                                (.fill box "darkgrey")
                                (.draw box)))]
    [konva-stage
     {:width js/window.innerWidth
      :height js/window.innerHeight}
     [konva-layer
      {:on-mouseover on-mouse-over
       :on-mouseout on-mouse-out}
      (for [x (range 0 (/ width box-size))
            y (range 0 (/ height box-size))]
        [konva-rect {:key (str x "-" y)
                     :x (* x box-size)
                     :y (* y box-size)
                     :width (- box-size 1)
                     :height (- box-size 1)
                     :fill "darkgrey"
                     :stroke "white"}])]]))