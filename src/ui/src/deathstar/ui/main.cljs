(ns deathstar.ui.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [clojure.string :as str]
   [cljs.reader :refer [read-string]]

   ;; reitit

   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rss]
   [reitit.frontend.controllers :as rfc]
   [spec-tools.data-spec :as ds]
   ;; Just for pretty printting the match
   [fipp.edn]

   ;; render

   [reagent.core :as r]
   [reagent.dom]
   ["react" :as React :refer [useEffect]]
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
   ["@ant-design/icons/ReloadOutlined" :default AntIconReloadOutlined]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [deathstar.ui.spec :as ui.spec]
   [deathstar.ui.chan :as ui.chan]

   [notepad.docker-from-browser1]))

(goog-define BAR_PORT 0)
(goog-define FOO_ORIGIN "")

#_(set! BAR_PORT (str (subs js/location.port 0 2) (subs (str BAR_PORT) 2)))
#_(set! FOO_ORIGIN "http://localhost:3001")

(def channels (merge
               (ui.chan/create-channels)))

(def state* (reagent.core/atom {}))

(defonce match (r/atom nil))


(defn home-page []
  [:div
   [:h2 "Welcome to frontend"]
   [:p "Look at console log for controller calls."]])

(defn item-page [match]
  (let [{:keys [path query]} (:parameters match)
        {:keys [id]} path]
    [:div
     [:ul
      [:li [:a {:href (rfe/href ::item {:id 1})} "Item 1"]]
      [:li [:a {:href (rfe/href ::item {:id 2} {:foo "bar"})} "Item 2"]]]
     (if id
       [:h2 "Selected item " id])
     (if (:foo query)
       [:p "Optional foo query param: " (:foo query)])]))

(defonce match (r/atom nil))

(defn current-page []
  [:div
   [:ul
    [:li [:a {:href (rfe/href ::frontpage)} "Frontpage"]]
    [:li
     [:a {:href (rfe/href ::item-list)} "Item list"]]]
   (if @match
     (let [view (:view (:data @match))]
       [view @match]))
   [:pre (with-out-str (fipp.edn/pprint @match))]])

(defn log-fn [& params]
  (fn [_]
    (apply js/console.log params)))

(def routes
  (rf/router
   ["/"
    [""
     {:name ::frontpage
      :view home-page
      :controllers [{:start (log-fn "start" "frontpage controller")
                     :stop (log-fn "stop" "frontpage controller")}]}]
    ["items"
      ;; Shared data for sub-routes
     {:view item-page
      :controllers [{:start (log-fn "start" "items controller")
                     :stop (log-fn "stop" "items controller")}]}

     [""
      {:name ::item-list
       :controllers [{:start (log-fn "start" "item-list controller")
                      :stop (log-fn "stop" "item-list controller")}]}]
     ["/:id"
      {:name ::item
       :parameters {:path {:id int?}
                    :query {(ds/opt :foo) keyword?}}
       :controllers [{:parameters {:path [:id]}
                      :start (fn [{:keys [path]}]
                               (js/console.log "start" "item controller" (:id path)))
                      :stop (fn [{:keys [path]}]
                              (js/console.log "stop" "item controller" (:id path)))}]}]]]
   {:data {:controllers [{:start (log-fn "start" "root-controller")
                          :stop (log-fn "stop" "root controller")}]
           :coercion rss/coercion}}))

#_(defn rc-current-page
  [channels state*]
  [rc-layout channels state*
   (if @match
     (let [page (:page (:data @match))]
       [page @match]))])


#_(defn create-proc-ops
    [channels ctx opts]
    (let [{:keys [::ui.chan/ops|]} channels
          {:keys [::ui.spec/state*]} ctx]
      (go
        (loop []
          (when-let [[value port] (alts! [ops|])]
            (condp = port
              ops|
              (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

                {::op.spec/op-key ::ui.chan/init
                 ::op.spec/op-type ::op.spec/fire-and-forget}
                (let [{:keys []} value]
                  (println ::init)
                  (ui.render/render-ui channels state* {}))

                {::op.spec/op-key ::ui.chan/update-state
                 ::op.spec/op-type ::op.spec/fire-and-forget}
                (let [{:keys []} value]
                  (swap! state* merge value))))

            (recur))))))

(defn ^:export main
  []
  (println ::main)
  (println ::BAR_PORT BAR_PORT)
  #_(create-proc-ops channels ctx {})
  (rfe/start!
   routes
   (fn [new-match]
     (swap! match (fn [old-match]
                    (if new-match
                      (assoc new-match :controllers (rfc/apply-controllers (:controllers old-match) new-match))))))
   {:use-fragment true})
  (reagent.dom/render [current-page] (.getElementById js/document "ui"))
  #_(reagent.dom/render [:f> rc-current-page channels state*]  (.getElementById js/document "ui"))
  #_(ui.chan/op
     {::op.spec/op-key ::ui.chan/init
      ::op.spec/op-type ::op.spec/fire-and-forget}
     channels
     {}))

(do (main))



(comment


  ; https://github.com/reagent-project/reagent/blob/master/CHANGELOG.md
; https://github.com/reagent-project/reagent/blob/master/examples/functional-components-and-hooks/src/example/core.cljs
; https://github.com/reagent-project/reagent/blob/master/doc/ReagentCompiler.md
; https://github.com/reagent-project/reagent/blob/master/doc/ReactFeatures.md



  (defn rc-menu
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
         [ant-menu-item {:key "/foo"}
          [:r> Link #js  {:to "/foo"} "foo"]]
         [ant-menu-item {:key "/foo/:name"}
          [:r> Link #js  {:to (format "/foo/%s" (subs (str (random-uuid)) 0 7))} "/foo/:name"]]])))

  (defn rc-layout
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


  (defn rc-main
    [channels state*]
    (r/with-let
      []
      [:> #_BrowserRouter HashRouter
       [:> Switch
        [:> Route {"path" "/"
                   "exact" true}
         [:f> rc-page-main channels state*]]
        [:> Route {"path" "/foo/:name"}
         [:f> rc-page-foo-name channels state*]]
        [:> Route {"path" "/foo"
                   "exact" true}
         [:f> rc-page-foo channels state*]]
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
         [ant-menu-item {:key "/foo"}
          [:r> Link #js  {:to "/foo"} "foo"]]
         [ant-menu-item {:key "/foo/:name"}
          [:r> Link #js  {:to (format "/foo/%s" (subs (str (random-uuid)) 0 7))} "/foo/:name"]]])))

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


  (defn rc-page-main
    [channels state*]
    (reagent.core/with-let
      []
      (let [_ (useEffect (fn []
                           (println ::rc-page-main-mount)
                           (fn useEffect-cleanup []
                             (println ::rc-page-main-unmount))))]
        [layout channels state*
         [:<>
          [:div ::rc-page-main]

          #_[:<>
             (if (empty? @state*)

               [:div "loading..."]

               [:<>
                [:pre {} (with-out-str (pprint @state*))]
                [ant-button {:icon (reagent.core/as-element [ant-icon-sync-outlined])
                             :size "small"
                             :title "button"
                             :on-click (fn [] ::button-click)}]])]]])))


  (defn rc-page-foo-name
    [channels state*]
    (reagent.core/with-let
      []
      (let [{:keys [:path :url :isExact :params]} (js->clj (useRouteMatch)
                                                           :keywordize-keys true)
            _ (useEffect (fn []
                           (do ::mount-smth)
                           (fn []
                             (do ::unmount-it))))]
        [layout channels state*
         [:div (str "/foo/" (:name params))]])))


  (defn rc-page-foo
    [channels state*]
    (reagent.core/with-let
      []
      (let []
        [layout channels state*
         [:<>
          [:div "rc-page-foo"]
          [:div
           [ant-row {}
            [ant-col
             [:r> Link #js  {:to "/foo/bar"} "/foo/bar"]]]
           [ant-row {}
            [ant-col
             [:r> Link #js  {:to "/foo/baz"} "/foo/baz"]]]]]])))


  (defn rc-page-not-found
    [channels state*]
    (reagent.core/with-let
      [layout channels state*
       [:<>
        [:div ::rc-page-not-found]]]))




;;  
  )
