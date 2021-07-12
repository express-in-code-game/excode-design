(ns deathstar.scenario.render
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
   [clojure.walk]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [deathstar.scenario.spec :as scenario.spec]
   [deathstar.scenario.chan :as scenario.chan]

   ["antd/lib/layout" :default AntLayout]
   ["antd/lib/menu" :default AntMenu]
   ["antd/lib/icon" :default AntIcon]
   ["antd/lib/button" :default AntButton]
   ["antd/lib/list" :default AntList]
   ["antd/lib/row" :default AntRow]
   ["antd/lib/col" :default AntCol]
   ["antd/lib/form" :default AntForm]
   ["antd/lib/input" :default AntInput]
   ["antd/lib/tabs" :default AntTabs]
   ["antd/lib/table" :default AntTable]
   ["react" :as React]
   ["antd/lib/checkbox" :default AntCheckbox]


   ["antd/lib/divider" :default AntDivider]
   ["@ant-design/icons/SmileOutlined" :default AntIconSmileOutlined]
   ["@ant-design/icons/LoadingOutlined" :default AntIconLoadingOutlined]
   ["@ant-design/icons/SyncOutlined" :default AntIconSyncOutlined]


   ["konva" :default Konva]
   ["react-konva" :as ReactKonva :rename {Stage KonvaStage
                                          Layer KonvaLayer
                                          Rect KonvaRect
                                          Path KonvaPath
                                          Circle KonvaCircle
                                          Group KonvaGroup
                                          Wedge KonvaWedge
                                          RegularPolygon KonvaRegularPolygon}]

   ["react-spring/renderprops-konva" :as ReactSpring :rename {animated ReactSpringAnimated
                                                              Spring ReactSpringSpring}]

   ["@flatten-js/core" :default flattenjs]))


(def ant-row (reagent.core/adapt-react-class AntRow))
(def ant-col (reagent.core/adapt-react-class AntCol))
(def ant-divider (reagent.core/adapt-react-class AntDivider))
(def ant-layout (reagent.core/adapt-react-class AntLayout))
(def ant-layout-content (reagent.core/adapt-react-class (.-Content AntLayout)))
(def ant-layout-header (reagent.core/adapt-react-class (.-Header AntLayout)))

(def ant-menu (reagent.core/adapt-react-class AntMenu))
(def ant-menu-item (reagent.core/adapt-react-class (.-Item AntMenu)))
(def ant-icon (reagent.core/adapt-react-class AntIcon))
(def ant-button (reagent.core/adapt-react-class AntButton))
(def ant-button-group (reagent.core/adapt-react-class (.-Group AntButton)))
(def ant-list (reagent.core/adapt-react-class AntList))
(def ant-input (reagent.core/adapt-react-class AntInput))
(def ant-input-password (reagent.core/adapt-react-class (.-Password AntInput)))
(def ant-checkbox (reagent.core/adapt-react-class AntCheckbox))
(def ant-form (reagent.core/adapt-react-class AntForm))
(def ant-table (reagent.core/adapt-react-class AntTable))
(def ant-form-item (reagent.core/adapt-react-class (.-Item AntForm)))
(def ant-tabs (reagent.core/adapt-react-class AntTabs))
(def ant-tab-pane (reagent.core/adapt-react-class (.-TabPane AntTabs)))

(def ant-icon-smile-outlined (reagent.core/adapt-react-class AntIconSmileOutlined))
(def ant-icon-loading-outlined (reagent.core/adapt-react-class AntIconLoadingOutlined))
(def ant-icon-sync-outlined (reagent.core/adapt-react-class AntIconSyncOutlined))

; https://github.com/DeathStarGame/starnet/blob/af86204ff94776ceab140208f5a6e0d654d30eba/ui/src/starnet/ui/alpha/main.cljs
; https://github.com/DeathStarGame/starnet/blob/af86204ff94776ceab140208f5a6e0d654d30eba/ui/src/starnet/ui/alpha/render.cljs


(def konva-stage (reagent.core/adapt-react-class KonvaStage))
(def konva-layer (reagent.core/adapt-react-class KonvaLayer))
(def konva-rect (reagent.core/adapt-react-class KonvaRect))
(def konva-circle (reagent.core/adapt-react-class KonvaCircle))
(def konva-group (reagent.core/adapt-react-class KonvaGroup))
(def konva-path (reagent.core/adapt-react-class KonvaPath))
(def konva-wedge (reagent.core/adapt-react-class KonvaWedge))
(def konva-regular-polygon (reagent.core/adapt-react-class KonvaRegularPolygon))

(def konva-animated-circle (reagent.core/adapt-react-class (.-Circle ReactSpringAnimated)))
(def react-spring-spring (reagent.core/adapt-react-class ReactSpringSpring))

(defn create-state*
  [data]
  (reagent.core/atom data))

(declare  rc-main)

(defn render-ui
  [channels state* {:keys [id] :or {id "ui"}}]
  (reagent.dom/render [rc-main channels state*]  (.getElementById js/document id)))

(def colors
  {::scenario.spec/sands "#edd3af" #_"#D2B48Cff"
   ::scenario.spec/signal-tower "brown"
   ::scenario.spec/recharge "#30ad23"
   ::scenario.spec/rover "blue"})

(defn rc-background-layer
  [channels state*]
  (reagent.core/with-let
    [entities* (reagent.core/cursor state* [::scenario.spec/entities])
     box-size scenario.spec/box-size-px]
    [konva-layer
     {:id "background-layer"}
     [konva-rect {:width (* box-size scenario.spec/x-size)
                  :height (* box-size scenario.spec/y-size)
                  :id "background-rect"
                  :x 0
                  :y 0
                  :fill (::scenario.spec/sands colors)
                  :strokeWidth 0
                  :stroke "white"}]]))

(defn rc-terrain-grid-layer
  [channels state*]
  (reagent.core/with-let
    [entities* (reagent.core/cursor state* [::scenario.spec/entities])
     box-size scenario.spec/box-size-px]
    [konva-layer
     {:id "terrain"
      :on-mouseover (fn [evt]
                      (let [box (.-target evt)
                            entity (get @entities* (.id box))]
                        (swap! state* assoc ::scenario.spec/hovered-entity entity)
                        (.stroke box "white")
                        (.strokeWidth box 2)
                        (.draw box)))
      :on-mouseout (fn [evt]
                     (let [box (.-target evt)]
                       (.strokeWidth box 0.001)
                       (.stroke box false)
                       (.draw box)))}
     (for [x (range 0 scenario.spec/x-size)
           y (range 0 scenario.spec/y-size)]
       [konva-rect {:key (str x "-" y)
                    :width (- box-size 1)
                    :height (- box-size 1)
                    :id (str "sand-" x "-" y)
                    :x (* x box-size)
                    :y (* y box-size)
                    :fill (::scenario.spec/sands colors)
                    :strokeWidth 0.001
                    :stroke "white"}])]))

(defn rc-locations-layer
  [channels state*]
  (reagent.core/with-let
    [locations* (reagent.core/cursor state* [::scenario.spec/locations])
     entities-in-rovers-range* (reagent.core/cursor state* [::scenario.spec/entities-in-rovers-range])
     visited-locations* (reagent.core/cursor state* [::scenario.spec/visited-locations])
     box-size scenario.spec/box-size-px]
    (let [locations @locations*
          entities-in-rovers-range @entities-in-rovers-range*
          visited-locations @visited-locations*]
      [konva-layer
       {:on-mouseover (fn [evt]
                        (let [node (.-target evt)
                              location (get locations (.id node))
                              {:keys [::scenario.spec/x ::scenario.spec/y]} location
                              stage (.getStage node)
                              #_layer-terrain #_(.findOne stage "#terrain")
                              #_node-terrain #_(.findOne layer-terrain (str "#sand-" x "-" y))]
                          #_(println (.id box))
                          #_(println (get @entities* (.id box)))
                          (swap! state* assoc ::scenario.spec/hovered-entity location)
                          #_(println (js-keys box))
                          #_(println (.id box))
                          #_(.fill box "#E5FF80")
                          #_(.strokeWidth node 2)
                          #_(.stroke node "white")
                          #_(.brightness node 0)
                          #_(.scale node #js {:x 1.2 :y 1.2})
                          #_(.draw stage)
                          (.fill node "#E5FF80")
                          (.draw node)))
        :on-mouseout (fn [evt]
                       (let [node (.-target evt)
                             location (get locations (.id node))
                             {:keys [::scenario.spec/x ::scenario.spec/y]} location
                             stage (.getStage node)
                             #_layer-terrain #_(.findOne stage "#terrain")
                             #_node-terrain #_(.findOne layer-terrain (str "#sand-" x "-" y))]
                         #_(println (.id node-terrain))
                         #_(println (.id node))
                         #_(.fill box (::scenario.spec/color entity))
                         #_(.fill node-terrain "red")
                         (.fill node (get colors (::scenario.spec/entity-type location)))
                         #_(.draw node-terrain)
                         #_(.strokeWidth node 0.001)
                         #_(.stroke node "red")
                         #_(.scale node #js {:x 1 :y 1})
                         #_(.draw stage)
                         #_(.brightness node 0.5)
                         (.draw node)))}
       (map (fn [location]
              (let [{:keys [::scenario.spec/entity-type
                            ::scenario.spec/x
                            ::scenario.spec/y
                            ::scenario.spec/id
                            ::scenario.spec/color
                            ::scenario.spec/energy
                            ::scenario.spec/energy-min
                            ::scenario.spec/energy-max]} location
                    in-range? (boolean (get entities-in-rovers-range id))
                    visited-location? (boolean (get visited-locations id))]
                (when-not (= entity-type ::scenario.spec/sands)
                  (condp = entity-type

                    ::scenario.spec/signal-tower
                    [konva-wedge {:key (str x "-" y)
                                  :x (+ (* x box-size) (/ box-size 2) -0.5)
                                  :y (+ (* y box-size) (/ box-size 2) 2)
                                  :id id
                                  :radius (+ (/ box-size 2) (* (/ box-size 3) 
                                                               (/ (- energy energy-min)
                                                                  (- energy-max  energy-min))))
                                  :angle 50
                                  :rotation -115
                              ;; :filters #js [(.. Konva -Filters -Brighten)]
                                  :fill (if visited-location? "lightblue" (get colors entity-type))
                                  :strokeWidth (cond
                                                 in-range? 1
                                                 :else 0.001)
                                  :stroke "white"}]
                   ;deafult
                    [react-spring-spring
                     {:native true
                      :key (str x "-" y)
                      :config {} #_{:duration 500}
                      :from {:opacity 1}
                      :to {:opacity (if visited-location? 0 1)}}
                     (fn [props]
                       (reagent.core/as-element
                        [konva-animated-circle
                         {:x (+ (* x box-size) (/ box-size 2) -0.5)
                          :y (+ (* y box-size) (/ box-size 2) -0.5)
                          :id id
                          :radius (+ (/ box-size 5) (* (/ box-size 5)
                                                       (/ (- energy energy-min)
                                                          (- energy-max  energy-min))))
                          :visible (if (= (.getValue (aget props "opacity")) 0) false true)  #_(not visited-location?)
                          :fill (get colors entity-type)
                          :strokeWidth (if in-range? 1 0.001)
                          :stroke "white"}]))
                     #_[konva-circle {:key (str x "-" y)
                                      :x (+ (* x box-size) (/ box-size 2) -0.5)
                                      :y (+ (* y box-size) (/ box-size 2) -0.5)
                                      :id id
                                      :radius 4
                              ;; :filters #js [(.. Konva -Filters -Brighten)]
                                      :fill (get colors entity-type)
                                      :strokeWidth (if in-range? 1 0.001)
                                      :stroke "white"}]])
                  #_[konva-rect {:key (str x "-" y)
                                 :x (+ (* x box-size) 2)
                                 :y (+ (* y box-size) 2)
                                 :id id
                                 :width (- box-size 5)
                                 :height (- box-size 5)
                              ;; :filters #js [(.. Konva -Filters -Brighten)]
                                 :fill (get colors entity-type)
                                 :strokeWidth 0.001
                                 :stroke "white"}]))) (vals locations))])))

(defn rc-rovers-layer
  [channels state*]
  (reagent.core/with-let
    [rovers* (reagent.core/cursor state* [::scenario.spec/rovers])
     box-size scenario.spec/box-size-px]
    (let [rovers @rovers*]
      [:<>
       [konva-layer
        {:on-mouseover (fn [evt]
                         (let [node (.-target evt)
                               entity (get rovers (.id node))
                               {:keys [::scenario.spec/x ::scenario.spec/y]} entity
                               stage (.getStage node)
                               layer-range (.findOne stage "#rover-range")]
                           (swap! state* assoc ::scenario.spec/hovered-entity entity)
                           #_(.show layer-range)
                           #_(.draw layer-range)
                           (.fill node "#E5FF80")
                           (.draw node)))
         :on-mouseout (fn [evt]
                        (let [node (.-target evt)
                              entity (get rovers (.id node))
                              {:keys [::scenario.spec/x ::scenario.spec/y]} entity
                              stage (.getStage node)
                              layer-range (.findOne stage "#rover-range")]
                          #_(.hide layer-range)
                          #_(.draw layer-range)
                          (.fill node (get colors (::scenario.spec/entity-type entity)))
                          (.draw node)))}
        (map (fn [rover]
               (let [{:keys [::scenario.spec/x
                             ::scenario.spec/y
                             ::scenario.spec/id
                             ::scenario.spec/rover-vision-range
                             ::scenario.spec/energy-level]} rover]
                 [konva-group
                  {:key id}
                  [react-spring-spring
                   {:native true
                    :config {} #_{:duration 500}
                    :from {:x (+ (* x box-size) (/ box-size 2) -0.5)
                           :y (+ (* y box-size) (/ box-size 2) -0.5)}
                    :to {:x (+ (* x box-size) (/ box-size 2) -0.5)
                         :y (+ (* y box-size) (/ box-size 2) -0.5)}}
                   (fn [props]
                     (reagent.core/as-element
                      [konva-animated-circle
                       {:x (aget props "x")
                        :y (aget props "y")
                        :id id
                        :radius (/ box-size 3)
                        :fill (if (not= 0 energy-level)
                                (get colors ::scenario.spec/rover)
                                "red")
                        :strokeWidth 0
                        :stroke "white"}]))]
                  [react-spring-spring
                   {:native true
                    :config {} #_{:duration 500}
                    :from {:x (+ (* x box-size) (/ box-size 2) -0.5)
                           :y (+ (* y box-size) (/ box-size 2) -0.5)
                           :radius (* box-size rover-vision-range)}
                    :to {:x (+ (* x box-size) (/ box-size 2) -0.5)
                         :y (+ (* y box-size) (/ box-size 2) -0.5)
                         :radius (* box-size rover-vision-range)}}
                   (fn [props]
                     (reagent.core/as-element
                      [konva-animated-circle
                       {:x (aget props "x")
                        :y (aget props "y")
                        :id id
                        :radius (aget props "radius")
                        :strokeWidth 1
                        :strokeHitEnabled false
                        :fillEnabled false
                        :stroke "darkblue"}]
                      #_[konva-circle {:x (+ (* x box-size) (/ box-size 2) -0.5)
                                       :y (+ (* y box-size) (/ box-size 2) -0.5)
                                       :id id
                                       :radius (* box-size rover-vision-range)
                                       :strokeWidth 1
                                       :strokeHitEnabled false
                                       :fillEnabled false
                                       :stroke "darkblue"}]))]
                  [react-spring-spring
                   {:native true
                    :config {} #_{:duration 500}
                    :from {:x (+ (* x box-size) (/ box-size 2) -0.5)
                           :y (+ (* y box-size) (/ box-size 2) -0.5)
                           :radius (* box-size (/ energy-level 10))}
                    :to {:x (+ (* x box-size) (/ box-size 2) -0.5)
                         :y (+ (* y box-size) (/ box-size 2) -0.5)
                         :radius (* box-size (/ energy-level 10))}}
                   (fn [props]
                     (reagent.core/as-element
                      [konva-animated-circle
                       {:x (aget props "x")
                        :y (aget props "y")
                        :id id
                        :visible (not= 0 energy-level)
                        :radius (aget props "radius") #_(* box-size (/ energy-level 10))
                        :strokeWidth 1
                        :strokeHitEnabled false
                        :fillEnabled false
                        :stroke "yellow"
                        :opacity 0.5}]))]])) (vals rovers))]])))

(defn rc-stage
  [channels state*]
  (reagent.core/with-let
    [box-size scenario.spec/box-size-px]
    [konva-stage
     {:width (* box-size scenario.spec/x-size)
      :height (* box-size scenario.spec/y-size)}
     [rc-background-layer channels state*]
     #_[rc-terrain-grid-layer channels state*]
     [rc-locations-layer channels state*]
     [rc-rovers-layer channels state*]]))



(defn rc-entity
  [channels state*]
  (reagent.core/with-let
    [hovered-entity* (reagent.core/cursor state* [::scenario.spec/hovered-entity])]
    (let [hovered-entity @hovered-entity*]
      [:div {:style {:position "absolute"
                     :top (+ 20
                             (* scenario.spec/box-size-px scenario.spec/y-size))
                     :left 0
                     :max-width "464px"
                     :background-color "#ffffff99"}}
       [:pre
        (with-out-str (pprint
                       (-> hovered-entity
                           (clojure.walk/stringify-keys)
                           (clojure.walk/keywordize-keys))))]])))

(defn rc-stats
  [channels state*]
  (reagent.core/with-let
    [visited-locations* (reagent.core/cursor state* [::scenario.spec/visited-locations])
     rovers* (reagent.core/cursor state* [::scenario.spec/rovers])]
    (let [visited-locations @visited-locations*
          rovers @rovers*
          visited-signal-towers (filter (fn [[k location]]
                                          (= (::scenario.spec/entity-type location)
                                             ::scenario.spec/signal-tower)) visited-locations)]
      [:div {:style {:position "absolute"
                     :top (+ 20
                             (* scenario.spec/box-size-px scenario.spec/y-size))
                     :right 16
                     :max-width "464px"
                     :background-color "#ffffff99"}}
       [:div
        (format "count rovers: %s" (count rovers))]
       [:div
        (format "visited-signal-towers: %s" (count visited-signal-towers))]
       #_[:pre

          (with-out-str (pprint
                         (-> {::scenario.spec/count-rovers (count rovers)
                              ::scenario.spec/visited-signal-towers (count visited-signal-towers)}
                             (clojure.walk/stringify-keys)
                             (clojure.walk/keywordize-keys))))]])))

(defn rc-main
  [channels state*]
  (reagent.core/with-let []
    [:<>
     [:div "Rovers on Mars"]
     #_[:pre {} (with-out-str (pprint @state*))]
     #_[ant-button {:icon (reagent.core/as-element [ant-icon-sync-outlined])
                    :size "small"
                    :title "button"
                    :on-click (fn [] ::button-click)}]
     #_[rc-grid channels state*]
     [rc-stage channels state*]
     [rc-entity channels state*]
     [rc-stats channels state*]

     #_[lab.render.konva/rc-konva-grid channels state*]
     #_[lab.render.konva/rc-konva-example-circle channels state*]]))