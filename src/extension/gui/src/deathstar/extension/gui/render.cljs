(ns deathstar.extension.gui.render
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]
   [reagent.core :as r]
   [reagent.dom :as rdom]

   [deathstar.extension.spec :as spec]
   [deathstar.multiplayer.remote.spec :as remote.spec]
   
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
   ["@ant-design/icons/SmileOutlined" :default AntSmileOutlined]))

(def ant-row (r/adapt-react-class AntRow))
(def ant-col (r/adapt-react-class AntCol))
(def ant-divider (r/adapt-react-class AntDivider))

(def ant-layout (r/adapt-react-class AntLayout))
(def ant-layout-content (r/adapt-react-class (.-Content AntLayout)))
(def ant-layout-header (r/adapt-react-class (.-Header AntLayout)))
(def ant-smile-outlined (r/adapt-react-class AntSmileOutlined))
(def ant-menu (r/adapt-react-class AntMenu))
(def ant-menu-item (r/adapt-react-class (.-Item AntMenu)))
(def ant-icon (r/adapt-react-class AntIcon))
(def ant-button (r/adapt-react-class AntButton))
(def ant-list (r/adapt-react-class AntList))
(def ant-input (r/adapt-react-class AntInput))
(def ant-input-password (r/adapt-react-class (.-Password AntInput)))
(def ant-checkbox (r/adapt-react-class AntCheckbox))
(def ant-form (r/adapt-react-class AntForm))
(def ant-table (r/adapt-react-class AntTable))
(def ant-form-item (r/adapt-react-class (.-Item AntForm)))
(def ant-tabs (r/adapt-react-class AntTabs))
(def ant-tab-pane (r/adapt-react-class (.-TabPane AntTabs)))

(declare rc-main)

(defn create-state
  [data]
  (r/atom data))

(defn- render-ui
  [channels ctx {:keys [id] :or {id "ui"}}]
  (rdom/render [rc-main channels ctx]  (.getElementById js/document id)))

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
  (put! (::ops| channels) {:op ::render :id dom-element-id}))


(def rc-tab-connections-columns
  [{:title "Settings file"
    :key :settings
    :dataIndex (str ::spec/settings-filepath)}
   {:title "Status"
    :key :status
    :dataIndex (str ::remote.spec/connection-status)}
   {:title "Actions"
    :key "action"
    :width "48px"
    :render (fn [txt rec idx]
              (r/as-element
               [ant-button-group
                {:size "small"}
                [ant-button
                 {;:icon "plus"
                  :type "primary"
                  :on-click #(rf/dispatch
                              [::evs/select-feature
                               rec])}
                 "connect"]
                [ant-button
                 {;:icon "plus"
                  :type "primary"

                  :on-click #(rf/dispatch
                              [::evs/select-feature
                               rec])}
                 "disconnect"]]))}
   #_{:title ""
      :key "empty"}])

(defn rc-tab-connections
  [channels ctx]
  (r/with-let [data (r/cursor (ctx :state) [:data])
               counter (r/cursor (ctx :state) [:counter])]
    [ant-table {:show-header true
                :size "small"
                :row-key :name
                :style {:height "30%" :overflow-y "auto"}
                :columns rc-tab-connections-columns
                :dataSource data
                :scroll {:y 216}
                :pagination false
                :rowSelection {:selectedRowKeys []
                               :on-change
                               (fn [ks rows ea]
                                 (println ks))}}]))

(defn rc-tab-state
  [channels ctx]
  (r/with-let [data (r/cursor (ctx :state) [:data])
               counter (r/cursor (ctx :state) [:counter])]
    [:<>
     [:pre {} (with-out-str (pprint @state))]]))

(defn rc-main
  [{:keys [input|] :as channels} ctx]
  (r/with-let [data (r/cursor (ctx :state) [:data])
               counter (r/cursor (ctx :state) [:counter])]
    (if (empty? @state)

      [:div "loading..."]

      [:<>
       [ant-tabs {:defaultActiveKey :connections}
        [ant-tab-pane {:tab "Connections" :key :connections}
         [rc-tab-connections channels ctx]]
        [ant-tab-pane {:tab "Multiplayer" :key :multiplayer}
         [:div  ::multiplayer]]
        [ant-tab-pane {:tab "State" :key :state}
         [rc-tab-state channels ctx]]]]
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


