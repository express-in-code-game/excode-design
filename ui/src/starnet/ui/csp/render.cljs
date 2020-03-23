(ns starnet.ui.csp.render
  (:require
   [clojure.repl :refer [doc]]
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
   ["antd/lib/divider" :default AntDivider]
   ["@ant-design/icons/SmileOutlined" :default AntSmileOutlined]))

(def ant-row (r/adapt-react-class AntRow))
(def ant-col (r/adapt-react-class AntCol))
(def ant-divider (r/adapt-react-class AntDivider))

(def ant-layout (r/adapt-react-class AntLayout))
(def ant-layout-content (r/adapt-react-class (.-Content AntLayout)))
(def ant-layout-header (r/adapt-react-class (.-Header AntLayout)))
(def ant-menu (r/adapt-react-class AntMenu))
(def ant-menu-item (r/adapt-react-class (.-Item AntMenu)))
(def ant-icon (r/adapt-react-class AntIcon))
(def ant-button (r/adapt-react-class AntButton))
(def ant-list (r/adapt-react-class AntList))
(def ant-smile-outlined (r/adapt-react-class AntSmileOutlined))


(declare rc-ui)

(def ^:private ratoms (let []
                        {:state (r/atom {})}))

(defn proc-derived-state-ui
  [{:keys [ml-router ch-derived-state-ui ml-http-res]}]
  (let [c-router (chan 1)
        c-http (chan 1)]
    (tap ml-router c-router)
    (tap ml-http-res  c-http)
    (go (loop [s nil]
          (if-let [[v port] (alts! [c-router])]
            (condp = port
              c-router (let [u (select-keys v [:router/handler :history/pushed])
                             s (merge s u)]
                         (println "proc-derived-state-ui" s)
                         (put! ch-derived-state-ui s)

                         (recur s)))))
        (println "closing proc-derived-state-ui"))))

(defn proc-render-ui
  [{:keys [] :as channels}]
  (let [c-dsu (chan 1)
        root-el (.getElementById js/document "ui")]
    (go (loop []
          (let [{:keys [router/handler history/pushed] :as v} (<! c-dsu)]
            (println (gstring/format "rendering %s" handler))
            (condp = handler
              :page/events (do
                             (render/page-events root-el channels v)
                             (recur))
              :page/games (do
                            (render/page-games root-el channels v)
                            (recur))
              :page/user-games (do
                                 (render/page-user-games root-el channels v)
                                 (recur))
              :page/userid-games (do
                                   (render/page-userid-games root-el channels v)
                                   (recur))
              :page/userid (do
                             (render/page-userid root-el channels v)
                             (recur))
              (do
                (render/not-found root-el channels v)
                (recur)))))
        (println "closing proc-render"))))


(defn menu
  [{:keys [router/handler history/pushed]}]
  (let []
    (fn []
      [ant-menu {:theme "light"
                 :mode "horizontal"
                 :size "small"
                 :style {:lineHeight "32px"}
                 :default-selected-keys ["home-panel"]
                 :selected-keys [handler]
                 :on-select (fn [x] (do))}
       [ant-menu-item {:key :page/events}
        [:a {:href "/events"} "events"]]
       [ant-menu-item {:key :page/games}
        [:a {:href "/games"} "games"]]
       [ant-menu-item {:key :page/user-games}
        [:a {:href "u/games"} "u/games"]]
       [ant-menu-item {:key :page/settings}
        [:a {:href "/settings"} "settings"]]
       [ant-menu-item {:key :page/userid}
        [:a {:href (gstring/format "/u/%s" (gen/generate gen/string-alphanumeric))} "user/random"]]
       [ant-menu-item {:key :page/non-existing}
        [:a {:href (gstring/format "/non-existing" (gen/generate gen/string-alphanumeric))} "non-existing"]]
       [ant-menu-item {:key :page/login}
        [:a {:href "/login"} "login"]]])))

(defn layout
  [content]
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
    [menu]]
   [ant-layout-content {:class "main-content"
                        :style {:margin-top "32px"
                                :padding "32px 32px 32px 32px"}}
    content]])

(defn layout-game
  [content]
  [ant-layout {:style {:min-height "100vh"}}
   [ant-layout-content {:class "main-content"
                        :style {:margin-top "32px"
                                :padding "32px 32px 32px 32px"}}
    content]]
  )

(defn rc-page-events
  [channels ratoms]
  (let []
    (fn [channels ratoms]
      (let []
        [layout
         [:<>
          [:div "rc-page-events"]]]))))

(defn rc-page-settings
  [channels ratoms]
  (let []
    (fn [channels ratoms]
      (let []
        [layout
         [:<>
          [:div "rc-page-settings"]]]))))

(defn rc-page-games
  [channels ratoms]
  (let []
    (fn [channels ratoms]
      (let []
        [layout
         [:<>
          [:div "rc-page-games"]]]))))

(defn rc-page-userid-games
  [channels ratoms]
  (let []
    (fn [channels ratoms]
      (let []
        [layout
         [:<>
          [:div "rc-page-userid-games"]]]))))

(defn rc-page-userid
  [channels ratoms]
  (let []
    (fn [channels ratoms]
      (let []
        [layout
         [:<>
          [:div "rc-page-userid"]]]))))

(defn rc-page-not-found
  [channels ratoms]
  (let []
    (fn [channels ratoms]
      (let []
        [layout
         [:<>
          [:div "rc-page-not-found"]]]))))

(defn rc-page-login
  [channels ratoms]
  (let []
    (fn [channels ratoms]
      (let []
        [layout
         [:<>
          [:div "rc-page-login"]]]))))

(defn rc-page-game
  [channels ratoms]
  (let []
    (fn [channels ratoms]
      (let []
        [layout
         [:<>
          [:div "rc-page-game"]]]))))

(defn rc-page-user-games
  [channels ratoms]
  (let []
    (fn [channels ratoms]
      (let []
        [layout
         [:<>
          [:div  "rc-page-user-games"]
          [ant-button {:value "button" :size "small"} "button"]
          [:div
           [:a {:target "_blank"
                :href (gstring/format "/game/%s" (str (gen/generate gen/uuid)))}
            [ant-button {:size "small"} "create game"]]]]
         ]))))

(defn rc-ui
  [channels ratoms]
  (let [{:keys [router/handler history/pushed]} @(ratoms :state)]
    (fn [channels ratoms]
      (println (gstring/format "rendering %s" handler))
      (let []
        (condp = handler
          :page/events [rc-page-events channels ratoms]
          :page/games [rc-page-games channels ratoms]
          :page/user-games [rc-page-user-games channels ratoms]
          :page/userid-games [rc-page-userid-games channels ratoms]
          :page/userid [rc-page-userid channels ratoms]
          [rc-page-not-found channels ratoms])))))

#_(defn ui-header
    [channels state]
    (let [{:keys [history/pushed]} state
          {:keys [handler]} pushed]
      [:header {:class "ui-header" :style {:display "flex"}}
       [:div "starnet"]
       [:a {:href "/events"} "events"]
       [:br]
       [:a {:href "/games"} "games"]
       [:a {:href "u/games"} "u/games"]
       [:a {:href "/settings"} "settings"]
       [:a {:href (gstring/format "/u/%s" (gen/generate gen/string-alphanumeric))} "user/random"]
       [:a {:href (gstring/format "/non-existing" (gen/generate gen/string-alphanumeric))} "not-found"]]))