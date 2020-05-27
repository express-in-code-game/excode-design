(ns ui.ext.layout.render
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]

   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]

   ["antd/lib/layout" :default AntLayout]
   ["antd/lib/menu" :default AntMenu]
   ["antd/lib/icon" :default AntIcon]
   ["antd/lib/button" :default AntButton]
   ["antd/lib/list" :default AntList]
   ["antd/lib/row" :default AntRow]
   ["antd/lib/col" :default AntCol]
   ["antd/lib/form" :default AntForm]
   ["antd/lib/input" :default AntInput]
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
(def ant-form-item (r/adapt-react-class (.-Item AntForm)))

(defn menu
  [channels ratom]
  (let [handler* (r/cursor ratom [:handler])
        url* (r/cursor ratom [:url])] ; for test, remove
    (fn [_ _]
      (let [handler @handler*
            url @url*]
        [ant-menu {:theme "light"
                   :mode "horizontal"
                   :size "small"
                   :style {:lineHeight "32px"}
                   :default-selected-keys ["home-panel"]
                   :selected-keys [handler]
                   :on-select (fn [x] (do))}
         [ant-menu-item {:key :ext-events}
          [:a {:href "/events"} "events"]]
         [ant-menu-item {:key :ext-games}
          [:a {:href "/games"} "games"]]
         [ant-menu-item {:key :ext-starnet}
          [:a {:href (gstring/format "/game/%s" (gen/generate gen/string-alphanumeric))} "game/:uuid"]]
         [ant-menu-item {:key :ext-stats-user}
          [:a {:href (gstring/format "/stats/%s" (gen/generate gen/string-alphanumeric))} "stats/:username"]]
         [ant-menu-item {:key :ext-settings}
          [:a {:href "/settings"} "settings"]]]))))

(defn layout
  [channels ratom #_content]
  [ant-layout {:style {:min-height "100vh"}}
   [ant-layout-header
    {:style {:position "fixed"
             :z-index 1
             :lineHeight "32px"
             :height "32px"
             :padding 0
             :background "#000" #_"#001529"
             :width "100%"}}
    [:a {:href "/"
         :class "logo"}
     #_[:img {:class "logo-img" :src "./img/logo-4.png"}]
     [:div {:class "logo-name"} "starnet"]]
    [menu channels ratom]
    [:div {:id "sign-in"
           :style {:position "absolute" :top 0 :right 20}} "sign-in"]
    #_[rc-user-identity channels ratom]]
   [ant-layout-content {:id "content"
                        :class "main-content"
                        :style {:margin-top "32px"
                                :padding "32px 32px 32px 32px"}}
    #_content]])

(defn render
  [channels ratom]
  (rdom/render [layout channels ratom] (.getElementById js/document "ui")))