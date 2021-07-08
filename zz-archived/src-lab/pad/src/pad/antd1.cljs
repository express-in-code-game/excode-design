(ns pad.antd1
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
   ["@ant-design/icons/ReloadOutlined" :default AntIconReloadOutlined]))


; https://github.com/sergeiudris/starnet/blob/af86204ff94776ceab140208f5a6e0d654d30eba/ui/src/starnet/ui/alpha/main.cljs
; https://github.com/sergeiudris/starnet/blob/af86204ff94776ceab140208f5a6e0d654d30eba/ui/src/starnet/ui/alpha/render.cljs

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
(def ant-icon-reload-outlined (reagent.core/adapt-react-class AntIconReloadOutlined))



#_(defn rc-main
    [{:keys [input|] :as channels} state]
    (reagent.core/with-let [data (reagent.core/cursor state [:data])
                 counter (reagent.core/cursor state [:counter])]
      (if (empty? @state)

        [:div "loading..."]

        [:<>
         [:pre {} (with-out-str (pprint @state))]
         [ant-button {:icon (reagent.core/as-element [ant-icon-sync-outlined])
                      :size "small"
                      :title "button"
                      :on-click (fn [] ::button-click)}]]

        #_[:<>
           [ant-tabs {:defaultActiveKey :connections}
            [ant-tab-pane {:tab "Connections" :key :connections}
             [rc-tab-connections channels state]]
            [ant-tab-pane {:tab "Multiplayer" :key :multiplayer}
             [:div  ::multiplayer]]
            [ant-tab-pane {:tab "State" :key :state}
             [rc-tab-state channels state]]]]
        #_[:<>
           #_[:div {} "rc-main"]
           #_[:button {:on-click (fn [e]
                                   (println "button clicked")
                                   #_(put! ops| ???))} "button"]
           #_[:div ":conf"]
           #_[:div {} (with-out-str (pprint @conf))]
           #_[:div @lrepl-id]
           #_[:div @ns-sym]
           [:br]
           [:div ":counter"]
           [:div {} (str @counter)]
           [:input {:type "button" :value "counter-inc"
                    :on-click #(swap! (ctx :state) update :counter inc)}]
           [:br]
           [:div ":data"]
           [:section
            (map-indexed (fn [i v]
                           ^{:key i} [:pre {} (with-out-str (pprint v))])
                         @data)]])))