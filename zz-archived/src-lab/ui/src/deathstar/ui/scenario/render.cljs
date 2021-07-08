(ns deathstar.ui.scenario.render
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

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]


   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]

   [deathstar.ui.spec :as ui.spec]

   [deathstar.scenario-api.spec :as scenario-api.spec]
   [deathstar.scenario-api.chan :as scenario-api.chan]

   ["react" :as React]
   ["react-router-dom" :as ReactRouter :refer [BrowserRouter
                                               HashRouter
                                               Switch
                                               Route
                                               Link
                                               useLocation
                                               useHistory
                                               useRouteMatch
                                               useParams]]
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

   ["antd/lib/checkbox" :default AntCheckbox]


   ["antd/lib/divider" :default AntDivider]
   ["@ant-design/icons/SmileOutlined" :default AntIconSmileOutlined]
   ["@ant-design/icons/LoadingOutlined" :default AntIconLoadingOutlined]
   ["@ant-design/icons/SyncOutlined" :default AntIconSyncOutlined]
   ["@ant-design/icons/ReloadOutlined" :default AntIconReloadOutlined]))

(do
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
  (def ant-icon-reload-outlined (reagent.core/adapt-react-class AntIconReloadOutlined)))


(defn rc-iframe
  [channels state* opts-iframe]
  (reagent.core/with-let
    [force-updater (reagent.core/atom (random-uuid))]
    [:div {:style {} #_{:display "none"}}
     [ant-row
      [ant-button {:icon (reagent.core/as-element [ant-icon-reload-outlined])
                   :size "small"
                   :title "button"
                   :on-click (fn [] (reset! force-updater (random-uuid)))}]]
     [ant-row
      [:iframe (merge
                {:src "http://localhost:11950/render.html"
                 :key @force-updater
                 :width "100%"
                 :height "400"}
                opts-iframe)]]]))


(defn rc-iframe-scenario
  [channels state*]
  (reagent.core/with-let
    [force-updater (reagent.core/atom (random-uuid))
     scenario-origin (reagent.core/cursor state* [::ui.spec/scenario-origin])]
    [:<>
     [ant-row
      [ant-button-group
       {:size "small"}
       [ant-button {:icon (reagent.core/as-element [ant-icon-reload-outlined])
                    :size "small"
                    :title "reload page"
                    :on-click (fn [] (reset! force-updater (random-uuid)))}]
       [ant-button {:size "small"
                    :title "generate"
                    :on-click (fn []
                                (scenario-api.chan/op
                                 {::op.spec/op-key ::scenario-api.chan/generate
                                  ::op.spec/op-type ::op.spec/fire-and-forget}
                                 channels
                                 {}))} "generate"]
       [ant-button {:size "small"
                    :title "reset"
                    :on-click (fn []
                                (scenario-api.chan/op
                                 {::op.spec/op-key ::scenario-api.chan/reset
                                  ::op.spec/op-type ::op.spec/fire-and-forget}
                                 channels
                                 {}))} "reset"]
       [ant-button {:size "small"
                    :title "resume"
                    :on-click (fn []
                                (scenario-api.chan/op
                                 {::op.spec/op-key ::scenario-api.chan/resume
                                  ::op.spec/op-type ::op.spec/fire-and-forget}
                                 channels
                                 {}))} "resume"]
       [ant-button {:size "small"
                    :title "pause"
                    :on-click (fn []
                                (scenario-api.chan/op
                                 {::op.spec/op-key ::scenario-api.chan/pause
                                  ::op.spec/op-type ::op.spec/fire-and-forget}
                                 channels
                                 {}))} "pause"]
       [ant-button {:size "small"
                    :title "replay"
                    :on-click (fn []
                                (scenario-api.chan/op
                                 {::op.spec/op-key ::scenario-api.chan/replay
                                  ::op.spec/op-type ::op.spec/fire-and-forget}
                                 channels
                                 {}))} "replay"]
       [ant-button {:size "small"
                    :title "next-step"
                    :on-click (fn []
                                (scenario-api.chan/op
                                 {::op.spec/op-key ::scenario-api.chan/next-step
                                  ::op.spec/op-type ::op.spec/fire-and-forget}
                                 channels
                                 {}))} "next-step"]]]
     [ant-row {:style {:height "100%"}}
      [ant-tabs {:style {:width "100%"
                         :height "100%"}
                 :defaultActiveKey "player"}
       [ant-tab-pane {:style {:width "100%"
                              :height "100%"}
                      :tab "player" :key "player"}
        [:iframe {:src (format "%s/scenario.html" @scenario-origin)
                  :key @force-updater
                  :width "100%"
                  :height "100%"}]]
       [ant-tab-pane {:style {:width "100%"
                              :height "100%"}
                      :tab "peers" :key "peers"}
        #_[:iframe {:src (format "%s/scenario.html" @scenario-origin)
                    :key @force-updater
                    :width "100%"
                    :height "100%"}]]]]]))

(defn rc-page
  [channels state*]
  (reagent.core/with-let
    [scenario-origin (reagent.core/cursor state* [::ui.spec/scenario-origin])]
    [:<>
     [ant-row {:justify "center"
               :align "top" #_"middle"
               :style {:height "94%"}
                    ;; :gutter [16 24]
               }
      [ant-col {:span 8}
       #_[table-tournaments channels state*]]
      [ant-col {:span 16 :style {:height "100%"}}
       [rc-iframe-scenario channels state*]
       [ant-row {:justify "start"
                 :align "top" #_"middle"
                    ;; :gutter [16 24]
                 }
        [ant-col {:span 4}
         [rc-iframe channels state* {:width "80px"
                                     :height "32px"
                                     :src (format "%s/player.html" @scenario-origin)}]]]]]]))