(ns deathstar.ui.render
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

   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]
   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [deathstar.ui.spec :as ui.spec]
   [deathstar.ui.chan :as ui.chan]

   [deathstar.scenario-api.spec :as scenario-api.spec]
   [deathstar.scenario-api.chan :as scenario-api.chan]

   [deathstar.ui.scenario-play.render :as ui.scenario-play.render]

   ["react" :as React :refer [useEffect]]
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

(defn create-state*
  [data]
  (reagent.core/atom data))

(declare  rc-main
          rc-page-main
          rc-page-scenarios
          rc-page-scenarios-play
          rc-page-tournament
          rc-page-tournament-join
          rc-page-game
          rc-page-scenario
          rc-page-not-found)

(defn render-ui
  [channels state* {:keys [id] :or {id "ui"}}]
  (reagent.dom/render [:f> rc-main channels state*]  (.getElementById js/document id)))

; https://github.com/reagent-project/reagent/blob/master/CHANGELOG.md
; https://github.com/reagent-project/reagent/blob/master/examples/functional-components-and-hooks/src/example/core.cljs
; https://github.com/reagent-project/reagent/blob/master/doc/ReagentCompiler.md
; https://github.com/reagent-project/reagent/blob/master/doc/ReactFeatures.md

(defn rc-main
  [channels state*]
  (r/with-let
    []
    [:> #_BrowserRouter HashRouter
     [:> Switch
      [:> Route {"path" "/"
                 "exact" true}
       [:f> rc-page-main channels state*]]
      [:> Route {"path" "/scenarios/:name"}
       [:f> rc-page-scenarios-play channels state*]]
      [:> Route {"path" "/scenarios"}
       [:f> rc-page-scenarios channels state*]]
      [:> Route {"path" "/tournament/:frequency"}
       [:f> rc-page-tournament channels state*]]
      [:> Route {"path" "/tournament"
                 "exact" true}
       [:f> rc-page-tournament-join channels state*]]
      [:> Route {"path" "/game/:frequency"}
       [:f> rc-page-game channels state*]]
      [:> Route {"path" "/scenario/:frequency"}
       [:f> rc-page-scenario channels state*]]
      [:> Route {"path" "*"}
       [:f> rc-page-not-found channels state*]]]]))

(defn menu
  [channels state*]
  (reagent.core/with-let
    []
    (let [{:keys [:path :url :isExact :params]} (js->clj (useRouteMatch)
                                                         :keywordize-keys true)]
      [ant-menu {:theme "light"
                 :mode "horizontal"
                 :size "small"
                 :style {:lineHeight "32px"}
                 :default-selected-keys ["home-panel"]
                 :selected-keys [path]
                 :on-select (fn [x] (do))}
       [ant-menu-item {:key "/"}
        [:r> Link #js {:to "/"} "main"]]
       [ant-menu-item {:key "/scenarios"}
        [:r> Link #js  {:to "/scenarios"} "scenarios"]]
       [ant-menu-item {:key "/tournament"}
        [:r> Link #js  {:to "/tournament"} "tournament"]]
       [ant-menu-item {:key "/tournament/:frequency"}
        [:r> Link #js  {:to (format "/tournament/%s" (subs (str (random-uuid)) 0 7))} "/tournament/:frequency"]]
       [ant-menu-item {:key "/game/:frequency"}
        [:r> Link #js  {:to (format "/game/%s" (subs (str (random-uuid)) 0 7))} "/game/:frequency"]]
       [ant-menu-item {:key "/scenario/:frequency"}
        [:r> Link #js  {:to (format "/scenario/%s" (subs (str (random-uuid)) 0 7))} "/scenario/:frequency"]]])))

(defn layout
  [channels state* content]
  [ant-layout {:style {:min-height "100vh"}}
   [ant-layout-header
    {:style {:position "fixed"
             :z-index 1
             :lineHeight "32px"
             :height "32px"
             :padding 0
             :background "#000" #_"#001529"
             :width "100%"}}
    [:div {:href "/"
           :class "ui-logo"}
     #_[:img {:class "logo-img" :src "./img/logo-4.png"}]
     [:div {:class "logo-name"} "DeathStarGame"]]
    [:f> menu channels state*]]
   [ant-layout-content {:class "main-content"
                        :style {:margin-top "32px"
                                :padding "32px 32px 32px 32px"}}
    content]])

(defn table-tournaments-columns
  [channels state*]
  [{:title "frequency"
    :key ::app.spec/frequency
    :dataIndex "frequency"}
   #_{:title "preview"
      :key "preview"
      :render
      (fn [txt rec idx]
        (let [v (js/JSON.stringify (aget rec "properties"))]
          (reagent.core/as-element
           [:div {:title v
                  :style  {:white-space "nowrap"
                           :max-width "216px"
                           :overflow-x "hidden"}}
            v])))}])

(defn table-tournaments-columns-extra
  [channels state*]
  [{:title "action"
    :key "action"
    :width "256px"
    :render (fn [text record index]
              (let [frequency (aget record "frequency")
                    host-id (aget record "host-id")
                    own-peer-id (::app.spec/peer-id @state*)]
                (reagent.core/as-element
                 [:<>
                  [ant-button-group
                   {:size "small"}
                   (when (and
                          (get-in @state* [::app.spec/tournaments frequency ::app.spec/peer-metas own-peer-id])
                          (not (= host-id own-peer-id)))
                     [ant-button
                      {:type "default"
                       :on-click (fn [evt]
                                   (app.chan/op
                                    {::op.spec/op-key ::app.chan/leave-tournament
                                     ::op.spec/op-type ::op.spec/fire-and-forget}
                                    channels
                                    {::app.spec/frequency frequency}))}
                      "leave"])
                   (when (= host-id own-peer-id)
                     [ant-button
                      {:type "default"
                       :on-click (fn [evt]
                                   (app.chan/op
                                    {::op.spec/op-key ::app.chan/close-tournament
                                     ::op.spec/op-type ::op.spec/fire-and-forget}
                                    channels
                                    {::app.spec/frequency frequency}))}
                      "close"])
                   (when-not (get-in @state* [::app.spec/tournaments frequency ::app.spec/peer-metas own-peer-id])
                     [ant-button
                      {:type "default"
                       :on-click (fn [evt]
                                   (app.chan/op
                                    {::op.spec/op-key ::app.chan/join-tournament
                                     ::op.spec/op-type ::op.spec/fire-and-forget}
                                    channels
                                    {::app.spec/frequency frequency}))}
                      "join"])]
                  [:r> Link #js  {:to (format "/tournament/%s" frequency)
                                  :target "_blank"} "open tab"]])))}
   #_{:title ""
      :key "empty"}])


(defn table-tournaments
  [channels state*]
  (reagent.core/with-let
    [tournaments* (reagent.core/cursor state* [::app.spec/tournaments])
     columns (vec (concat (table-tournaments-columns channels state*) (table-tournaments-columns-extra channels state*)))]
    (let [data (vec (vals @tournaments*))
          total (count data)]
      (println data)
      [ant-table {:show-header true
                  :size "small"
                  :title (fn [] (reagent.core/as-element
                                 [ant-row
                                  [ant-col {:span 2}
                                   [:span "Tournaments"]]
                                  [ant-col {:span 22}
                                   [ant-button-group
                                    {:size "small"}
                                    [ant-button
                                     {:on-click (fn [evt]
                                                  (app.chan/op
                                                   {::op.spec/op-key ::app.chan/create-tournament
                                                    ::op.spec/op-type ::op.spec/fire-and-forget}
                                                   channels
                                                   {}))} "create"]]]]))
                  :row-key ::app.spec/frequency
                  :style {:height "50%" :width "100%"}
                  :columns columns
                  :dataSource data
                  :on-change (fn [pag fil sor ext]
                               #_(js->clj {:pagination pag
                                           :filters fil
                                           :sorter sor
                                           :extra ext} :keywordize-keys true))
                  :scroll {;  :x "max-content" 
                                ;  :y 256
                           }
                  :pagination false}])))

(defn table-peer-metas-columns
  [channels state*]
  [{:title "peer-id"
    :key ::app.spec/peer-id
    :dataIndex ::app.spec/peer-id}
   {:title "counter"
    :key ::app.spec/counter
    :dataIndex ::app.spec/counter}])

(defn table-peer-metas
  [channels state*]
  (reagent.core/with-let
    [peer-metas* (reagent.core/cursor state* [::app.spec/peer-metas])
     columns (vec (concat (table-peer-metas-columns channels state*)))]
    (let [dataSource (vec (vals @peer-metas*))
          total (count dataSource)]
      [ant-table {:show-header true
                  :size "small"
                  :row-key ::app.spec/peer-id
                  :style {:height "50%" :width "100%"}
                  :columns columns
                  :dataSource dataSource
                  :on-change (fn [pag fil sor ext]
                               #_(js->clj {:pagination pag
                                           :filters fil
                                           :sorter sor
                                           :extra ext} :keywordize-keys true))
                  :scroll {;  :x "max-content" 
                                ;  :y 256
                           }
                  :pagination false}])))


(defn rc-page-main
  [channels state*]
  (reagent.core/with-let
    [_ (useEffect (fn []
                    (println ::rc-page-main-mount)
                    (fn useEffect-cleanup []
                      (println ::rc-page-main-unmount))))]
    [layout channels state*
     [:<>
      [ant-row {:justify "center"
                :align "top" #_"middle"
                :style {:height "40%"}
                    ;; :gutter [16 24]
                }
       [ant-col {:span 24}
        [table-tournaments channels state*]]]
      [ant-row {:justify "center"
                :align "top" #_"middle"
                :style {:height "40%"}
                    ;; :gutter [16 24]
                }
       [ant-col {:span 24}
        [table-peer-metas channels state*]]]
      #_[:<>
         (if (empty? @state*)

           [:div "loading..."]

           [:<>
            [:pre {} (with-out-str (pprint @state*))]
            [ant-button {:icon (reagent.core/as-element [ant-icon-sync-outlined])
                         :size "small"
                         :title "button"
                         :on-click (fn [] ::button-click)}]])]]]))

(defn rc-page-tournament-join
  [channels state*]
  (reagent.core/with-let
    [[form] (vec (.useForm AntForm))
     on-join
     (fn []
       (let [value (.getFieldsValue form)]
         (-> form
             (.validateFields)
             (.then (fn [values]
                      (println ::on-join)
                      (println values)
                      #_(put! ch-inputs {:ch/topic :inputs/ops
                                         :ops/op :op/login
                                         :u/password (aget vs "password")
                                         :u/username (aget vs "username")})))
             (.catch (fn [errorInfo]
                       (println ::on-join-error)
                       (println errorInfo))))))
     on-create
     (fn []
       (let [value (.getFieldsValue form)]
         (-> form
             (.validateFields #js ["name"])
             (.then (fn [values]
                      (println ::on-create)
                      (println values)
                      #_(put! ch-inputs {:ch/topic :inputs/ops
                                         :ops/op :op/login
                                         :u/password (aget vs "password")
                                         :u/username (aget vs "username")})))
             (.catch (fn [errorInfo]
                       (println ::on-join-error)
                       (println errorInfo))))))]
    [layout channels state*
     [ant-form {:labelCol {:span 8} :wrapperCol {:span 24} :form form}
      [ant-form-item {:label nil
                      :name "name"
                      :wrapperCol {:offset 1 :span 12}
                      :rules [{:required true :message "required"}]}
       [ant-input {:placeholder "name"}]]
      [ant-form-item {:label nil
                      :name "frequency"
                      :wrapperCol {:offset 1 :span 12}
                      :rules [{:required true :message "required"}]}
       [ant-input-password {:placeholder "frequency (only when join)"}]]
      [ant-form-item {:wrapperCol {:offset 1 :span 12}}
       [ant-row {:justify "start"}
        [ant-col {:offset 0 :span 2}
         [ant-button {:type "default" :on-click on-join} "join"]]
        [ant-col {:offset 0 :span 2}
         [ant-button {:type "default" :on-click on-create} "create"]]]]]]))


(defn rc-page-tournament
  [channels state*]
  (reagent.core/with-let
    []
    (let [{:keys [:path :url :isExact :params]} (js->clj (useRouteMatch)
                                                         :keywordize-keys true)
          _ (useEffect (fn []
                         (println (:frequency params))
                         (ui.chan/op
                          {::op.spec/op-key ::ui.chan/mount-tournament
                           ::op.spec/op-type ::op.spec/request-response
                           ::op.spec/op-orient ::op.spec/request}
                          channels
                          {::app.spec/frequency (:frequency params)})
                         (fn []
                           (ui.chan/op
                            {::op.spec/op-key ::ui.chan/unmount-tournament
                             ::op.spec/op-type ::op.spec/request-response
                             ::op.spec/op-orient ::op.spec/request}
                            channels
                            {::app.spec/frequency (:frequency params)}))))]
      [layout channels state*
       [:div "empty"]])))

(defn rc-page-game
  [channels state*]
  (reagent.core/with-let
    []
    (let [{:keys [:path :url :isExact :params]} (js->clj (useRouteMatch)
                                                         :keywordize-keys true)
          _ (useEffect (fn []
                         (ui.chan/op
                          {::op.spec/op-key ::ui.chan/mount-game
                           ::op.spec/op-type ::op.spec/request-response
                           ::op.spec/op-orient ::op.spec/request}
                          channels
                          {})
                         (fn []
                           (ui.chan/op
                            {::op.spec/op-key ::ui.chan/unmount-game
                             ::op.spec/op-type ::op.spec/request-response
                             ::op.spec/op-orient ::op.spec/request}
                            channels
                            {}))))]
      [layout channels state*
       [:<>
        [:div ::rc-page-game]]])))

(defn rc-page-scenario
  [channels state*]
  (reagent.core/with-let
    []
    (let [{:keys [:path :url :isExact :params]} (js->clj (useRouteMatch)
                                                         :keywordize-keys true)
          _ (useEffect (fn []
                         (ui.chan/op
                          {::op.spec/op-key ::ui.chan/mount-scenario
                           ::op.spec/op-type ::op.spec/request-response
                           ::op.spec/op-orient ::op.spec/request}
                          channels
                          {})
                         (fn []
                           (ui.chan/op
                            {::op.spec/op-key ::ui.chan/unmount-scenario
                             ::op.spec/op-type ::op.spec/request-response
                             ::op.spec/op-orient ::op.spec/request}
                            channels
                            {}))))]
      [layout channels state*
       [:div "empty"]])))


(defn rc-page-scenarios
  [channels state*]
  (reagent.core/with-let
    []
    (let []
      [layout channels state*
       [:<>
        [:div "rc-page-scenarios"]
        [:div
         [ant-row {}
          [ant-col
           [:r> Link #js  {:to "/scenarios/rovers2"} "rovers2"]]]
         [ant-row {}
          [ant-col
           [:r> Link #js  {:to "/scenarios/starnet"} "starnet"]]]]]])))

(defn rc-page-scenarios-play
  [channels state*]
  (reagent.core/with-let
    [{:keys [:path :url :isExact :params]} (js->clj (useRouteMatch)
                                                    :keywordize-keys true)]
    (let []
      (println ::rc-page-scenarios-play (:name params))
      [layout channels state*
       [:f> ui.scenario-play.render/rc-page channels state*]])))

(defn rc-page-not-found
  [channels state*]
  (reagent.core/with-let
    [layout channels state*
     [:<>
      [:div ::rc-page-not-found]]]))

