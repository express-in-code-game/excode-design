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
   [deathstar.scenario.core :as scenario.core]

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
   ["antd/lib/progress" :default AntProgress]



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

   ["@flatten-js/core" :default flattenjs]
   ["react-spring/renderprops-konva" :as ReactSpring :rename {animated ReactSpringAnimated
                                                              Spring ReactSpringSpring}]))


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
(def ant-progress (reagent.core/adapt-react-class AntProgress))

(def ant-icon-smile-outlined (reagent.core/adapt-react-class AntIconSmileOutlined))
(def ant-icon-loading-outlined (reagent.core/adapt-react-class AntIconLoadingOutlined))
(def ant-icon-sync-outlined (reagent.core/adapt-react-class AntIconSyncOutlined))

; https://github.com/sergeiudris/starnet/blob/af86204ff94776ceab140208f5a6e0d654d30eba/ui/src/starnet/ui/alpha/main.cljs
; https://github.com/sergeiudris/starnet/blob/af86204ff94776ceab140208f5a6e0d654d30eba/ui/src/starnet/ui/alpha/render.cljs


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


(defn create-state
  [data]
  (reagent.core/atom data))

(declare  rc-main rc-grid rc-entity rc-rover)

(defn render-ui
  [channels state {:keys [id] :or {id "ui"}}]
  (reagent.dom/render [rc-main channels state]  (.getElementById js/document id)))

(defn rc-main
  [channels state]
  (reagent.core/with-let []
    [:<>
     #_[:pre {} (with-out-str (pprint @state))]
     #_[ant-button {:icon (reagent.core/as-element [ant-icon-sync-outlined])
                    :size "small"
                    :title "button"
                    :on-click (fn [] ::button-click)}]
     [rc-grid channels state]
     [rc-entity channels state]
     [rc-rover channels state]
     
     
     #_[lab.render.konva/rc-konva-grid channels state]
     #_[lab.render.konva/rc-konva-example-circle channels state]]))

(def colors
  {::scenario.core/sands "#e9c48c" #_"#f7e5a9" #_"#D2B48Cff"
   ::scenario.core/location "brown"
   ::scenario.core/recharge "#30ad23"
   ::scenario.core/rover "blue"
   ::scenario.core/the-ship #_"teal" "lightblue" #_"#87ceeb"})

(defn rc-the-ship
  [channels state]
  (reagent.core/with-let
    [box-size scenario.core/box-size-px
     the-ship* (reagent.core/cursor state [::scenario.core/the-ship])]
    (let [{:keys [::scenario.core/id
                  ::scenario.core/x
                  ::scenario.core/y]
           :as the-ship} @the-ship*]
      [konva-layer
       {:on-click (fn [evt]
                    (let [box (.-target evt)
                          entity the-ship]
                      (scenario.chan/op
                       {::op.spec/op-key ::scenario.chan/click-entity
                        ::op.spec/op-type ::op.spec/fire-and-forget}
                       channels
                       entity)))
        :on-mouseover (fn [evt]
                        (let [box (.-target evt)
                              entity the-ship]
                          (swap! state assoc ::scenario.core/hovered-entity entity)
                          (.stroke box "white")
                          (.strokeWidth box 3)
                          (.draw box)))
        :on-mouseout (fn [evt]
                       (let [box (.-target evt)]
                         (.strokeWidth box 2)
                         (.stroke box false)
                         (.draw box)))}
       [konva-regular-polygon {:x (+ (* x box-size) (/ box-size 2) -0.5)
                               :y (+ (* y box-size) (/ box-size 2) -0.5)
                               :id id
                               :sides 3
                               :radius 7
                               :fill (get colors ::scenario.core/the-ship)
                               :stroke "white"
                               :strokeWidth 2}]])))


(defn rc-grid
  [channels state]
  (reagent.core/with-let [entities* (reagent.core/cursor state [::scenario.core/entities])
               rover* (reagent.core/cursor state [::scenario.core/rover])
               entities-in-range* (reagent.core/cursor state [::scenario.core/entities-in-range])
               selected-entity* (reagent.core/cursor state [::scenario.core/selected-entity])
               visited-locations* (reagent.core/cursor state [::scenario.core/visited-locations])
              ;;  width js/window.innerWidth
              ;;  height js/window.innerHeight
               box-size scenario.core/box-size-px

               entity-on-mouse-over (fn [evt]
                                      (let [node (.-target evt)
                                            entity (or (get @entities* (.id node)) @rover*)
                                            {:keys [::scenario.core/x ::scenario.core/y]} entity
                                            stage (.getStage node)
                                            layer-terrain (.findOne stage "#terrain")
                                            node-terrain (.findOne layer-terrain (str "#sand-" x "-" y))]
                                        #_(println (.id box))
                                        #_(println (get @entities* (.id box)))
                                        (swap! state assoc ::scenario.core/hovered-entity entity)
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
               entity-on-mouse-out (fn [evt]
                                     (let [node (.-target evt)
                                           entity (or (get @entities* (.id node)) @rover*)
                                           {:keys [::scenario.core/x ::scenario.core/y]} entity
                                           stage (.getStage node)
                                           layer-terrain (.findOne stage "#terrain")
                                           node-terrain (.findOne layer-terrain (str "#sand-" x "-" y))]
                                       #_(println (.id node-terrain))
                                       #_(println (.id node))
                                       #_(.fill box (::scenario.core/color entity))
                                       #_(.fill node-terrain "red")
                                       (.fill node (get colors (::scenario.core/entity-type entity)))
                                       #_(.draw node-terrain)
                                       #_(.strokeWidth node 0.001)
                                       #_(.stroke node "red")
                                       #_(.scale node #js {:x 1 :y 1})
                                       #_(.draw stage)
                                       #_(.brightness node 0.5)
                                       (.draw node)))]
    (let [entities-in-range @entities-in-range*
          visited-locations @visited-locations*
          selected-entity @selected-entity*]
      [:<>
       [:div "Get to the Ship"]
       [konva-stage
        {:width (* box-size scenario.core/x-size)
         :height (* box-size scenario.core/y-size)}
        [konva-layer
         {:id "terrain"
          :on-click (fn [evt]
                      (let [box (.-target evt)
                            entity (get @entities* (.id box))]
                        (scenario.chan/op
                         {::op.spec/op-key ::scenario.chan/click-entity
                          ::op.spec/op-type ::op.spec/fire-and-forget}
                         channels
                         entity)))
          :on-dblclick (fn [evt]
                         (let [box (.-target evt)]
                           (scenario.chan/op
                            {::op.spec/op-key ::scenario.chan/click-entity
                             ::op.spec/op-type ::op.spec/fire-and-forget}
                            channels
                            nil)))
          :on-mouseover (fn [evt]
                          (let [box (.-target evt)
                                entity (or (get @entities* (.id box)) @rover*)]
                            (swap! state assoc ::scenario.core/hovered-entity entity)
                            (.stroke box "white")
                            (.strokeWidth box 2)
                            (.draw box)))
          :on-mouseout (fn [evt]
                         (let [box (.-target evt)]
                           (.strokeWidth box 0.001)
                           (.stroke box false)
                           (.draw box)))}
         
          (map (fn [entity]
                (let [{:keys [::scenario.core/entity-type
                              ::scenario.core/x
                              ::scenario.core/y
                              ::scenario.core/id
                              ::scenario.core/color]} entity
                      in-range? false]
                  [konva-rect {:key (str x "-" y)
                               :width (- box-size 1)
                               :height (- box-size 1)
                               :id id
                              ;;  :filters #js [(.. Konva -Filters -Brighten)]
                              ;;  :ref (fn [node]
                              ;;         (when node
                              ;;           (.cache node)
                              ;;           (.brightness node -1)))
                               :x (* x box-size)
                               :y (* y box-size)
                               :fill (::scenario.core/sands colors)
                               :strokeWidth 0.001
                               :stroke "white"}])) (vals @entities*))
         #_(for [x (range 0 scenario.core/x-size)
                 y (range 0 scenario.core/y-size)]
             [konva-rect {:key (str x "-" y)
                          :width (- box-size 1)
                          :height (- box-size 1)
                          :id (str "sand-" x "-" y)
                          :x (* x box-size)
                          :y (* y box-size)
                          :fill (::scenario.core/sands colors)
                          :strokeWidth 0.001
                          :stroke "white"}])]
        [konva-layer
         {:on-mouseover entity-on-mouse-over
          :on-mouseout entity-on-mouse-out
          :on-click (fn [evt]
                      (let [box (.-target evt)
                            entity (or (get @entities* (.id box)) @rover*)]
                        (scenario.chan/op
                         {::op.spec/op-key ::scenario.chan/click-entity
                          ::op.spec/op-type ::op.spec/fire-and-forget}
                         channels
                         entity)))}
         (map (fn [entity]
                (let [{:keys [::scenario.core/entity-type
                              ::scenario.core/x
                              ::scenario.core/y
                              ::scenario.core/id
                              ::scenario.core/energy
                              ::scenario.core/energy-min
                              ::scenario.core/energy-max
                              ::scenario.core/color]} entity
                      in-range? false
                      visited-location? (boolean (get visited-locations id))]
                  (when (and (not= entity-type ::scenario.core/sands))
                    (condp = entity-type

                      ::scenario.core/location
                      [konva-wedge {:key (str x "-" y)
                                    :x (+ (* x box-size) (/ box-size 2) -0.5)
                                    :y (+ (* y box-size) (/ box-size 2) 2)
                                    :id id
                                    :radius 7
                                    :angle 50
                                    :rotation -115
                              ;; :filters #js [(.. Konva -Filters -Brighten)]
                                    :fill (if visited-location? "teal" (get colors entity-type))
                                    :strokeWidth (if (= (::scenario.core/id selected-entity) id)
                                                   2
                                                   0)
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
                            :radius (+ 2 (* 3 (/ (- energy energy-min) (- energy-max  energy-min))))
                            :visible (if (= (.getValue (aget props "opacity")) 0) false true)  #_(not visited-location?)
                            :fill (get colors entity-type)
                            :strokeWidth (if (= (::scenario.core/id selected-entity) id)
                                           2
                                           0)
                            :stroke "white"}]))
                       #_[konva-circle {:key (str x "-" y)
                                        :x (+ (* x box-size) (/ box-size 2) -0.5)
                                        :y (+ (* y box-size) (/ box-size 2) -0.5)
                                        :id id
                                        :visible (not visited-location?)
                                        :radius (+ 2 (* 3 (/ (- energy energy-min) (- energy-max  energy-min))))
                              ;; :filters #js [(.. Konva -Filters -Brighten)]
                                        :fill (get colors entity-type)
                                        :strokeWidth (if (= (::scenario.core/id selected-entity) id)
                                                       2
                                                       0)
                                        :stroke "white"}]]
                      )
                    #_[konva-rect {:key (str x "-" y)
                                   :x (+ (* x box-size) 2)
                                   :y (+ (* y box-size) 2)
                                   :id id
                                   :width (- box-size 5)
                                   :height (- box-size 5)
                              ;; :filters #js [(.. Konva -Filters -Brighten)]
                                   :fill (get colors entity-type)
                                   :strokeWidth 0.001
                                   :stroke "white"}]))) (vals @entities*))]
        (let [{:keys [::scenario.core/x
                      ::scenario.core/y
                      ::scenario.core/id
                      ::scenario.core/rover-vision-range
                      ::scenario.core/energy-level]} @rover*]
          [:<>
           [rc-the-ship channels state]
           [konva-layer
            {:on-click (fn [evt]
                         (let [box (.-target evt)
                               entity (or (get @entities* (.id box)) @rover*)]
                           (scenario.chan/op
                            {::op.spec/op-key ::scenario.chan/click-entity
                             ::op.spec/op-type ::op.spec/fire-and-forget}
                            channels
                            entity)))
             :on-mouseover (fn [evt]
                             (let [node (.-target evt)
                                   entity @rover*
                                   {:keys [::scenario.core/x ::scenario.core/y]} entity
                                   stage (.getStage node)
                                   layer-range (.findOne stage "#rover-range")]
                               (swap! state assoc ::scenario.core/hovered-entity entity)
                               #_(.show layer-range)
                               #_(.draw layer-range)
                               (.fill node "#E5FF80")
                               (.draw node)))
             :on-mouseout (fn [evt]
                            (let [node (.-target evt)
                                  entity @rover*
                                  {:keys [::scenario.core/x ::scenario.core/y]} entity
                                  stage (.getStage node)
                                  layer-range (.findOne stage "#rover-range")]
                              #_(.hide layer-range)
                              #_(.draw layer-range)
                              (.fill node (get colors (::scenario.core/entity-type entity)))
                              (.draw node)))}
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
                  :radius 6
                  :fill (get colors ::scenario.core/rover)
                  :strokeWidth (if (= (::scenario.core/id selected-entity) id)
                                 2
                                 0)
                  :stroke "white"}]))]]
           [konva-layer
            {:id "rover-range"}
            (when (= (::scenario.core/entity-type selected-entity) ::scenario.core/rover)
              [react-spring-spring
               {:native true
                :config {} #_{:duration 500}
                :from {:x (+ (* x box-size) (/ box-size 2) -0.5)
                       :y (+ (* y box-size) (/ box-size 2) -0.5)
                       :radius (* box-size (/ energy-level 10))
                       }
                :to {:x (+ (* x box-size) (/ box-size 2) -0.5)
                     :y (+ (* y box-size) (/ box-size 2) -0.5)
                     :radius (* box-size (/ energy-level 10))}}
               (fn [props]
                 (reagent.core/as-element
                  [konva-animated-circle
                   {:x (aget props "x")
                    :y (aget props "y")
                    :id id
                    :radius (aget props "radius") #_(* box-size (/ energy-level 10))
                    :strokeWidth 1
                    :strokeHitEnabled false
                    :fillEnabled false
                    :stroke "darkblue"}]))])]])]])))

(defn rc-entity
  [channels state]
  (reagent.core/with-let [hovered-entity* (reagent.core/cursor state [::scenario.core/hovered-entity])]
    [:div {:style {:position "absolute" 
                   :top (+ 20
                         (* scenario.core/box-size-px scenario.core/y-size))
                   :left 0 
                   :max-width "464px"
                   :background-color "#ffffff99"}}
     [:pre
      (with-out-str (pprint
                     (-> @hovered-entity*
                         (clojure.walk/stringify-keys)
                         (clojure.walk/keywordize-keys))))]]))

(defn rc-rover
  [channels state]
  (reagent.core/with-let [rover* (reagent.core/cursor state [::scenario.core/rover])]
    (let [energy-level (get @rover* ::scenario.core/energy-level)]
      [:<>
       (when energy-level
         [ant-progress {:percent energy-level
                        :size "default" #_"small"
                        :format (fn [percent success-percent]
                                  (format "%.4f" percent))
                        :style {:position "absolute"
                                :width "300px"
                                :top (+ 20
                                        (* scenario.core/box-size-px scenario.core/y-size))
                                :left 464
                                :max-width "464px"
                                :background-color "#ffffff99"}}])
       [:div {:style {:position "absolute"
                      :top (+ 20
                              (* scenario.core/box-size-px scenario.core/y-size)
                              20
                              )
                      :left 464
                      :max-width "464px"
                      :background-color "#ffffff99"}}
        [:pre
         (with-out-str (pprint
                        (-> @rover*
                            (clojure.walk/stringify-keys)
                            (clojure.walk/keywordize-keys))))]]])))