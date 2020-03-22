(ns starnet.ui.csp.render
  (:require
   [clojure.repl :refer [doc]]
   [reagent.core :as r]
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

   ["antd/lib/button" :default AntButton]
   ["antd/lib/list" :default AntList]
   ["@ant-design/icons/SmileOutlined" :default AntSmileOutlined]
   ))


(def ant-button (r/adapt-react-class AntButton))
(def ant-list (r/adapt-react-class AntList))
(def ant-smile-outlined (r/adapt-react-class AntSmileOutlined))


(defn ui-header
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

(defn page-events
  [el channels state]
  (r/render [:<>
             [ui-header channels state]
             [:div {:id "div-1"} "page events"]] el))

(defn page-games
  [el channels state]
  (r/render [:<>
             [ui-header channels state]
             [:div {:id "div-1"} "page games"]] el))

(defn page-userid-games
  [el channels state]
  (r/render [:<>
             [ui-header channels state]
             [:div {:id "div-1"} "page user/name/games"]] el))

(defn page-user-games
  [el channels state]
  (r/render [:<>
             [ui-header channels state]
             [ant-button {:value "button" :size "small"} "button"]
             [:div {:id "div-1"} "page u/games"]] el))

(defn page-userid
  [el channels state]
  (r/render [:<>
             [ui-header channels state]
             [:div {:id "div-1"} "page userid"]] el))



(defn not-found
  [el channels state]
  (r/render [:<>
             [ui-header channels state]
             [:div {:id "div-1"} "not found"]] el))
