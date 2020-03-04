(ns ui.alpha.home.view
  (:require  [reagent.core :as r]
             [cljs.repl :as repl]
             [cljs.pprint :as pp]
             [re-frame.core :as rf]
             [ui.alpha.home.subs :as subs]
             [ui.alpha.home.evs :as evs]
             ["antd/lib/button" :default AntButton]
             ["@ant-design/icons/SmileOutlined" :default AntSmileOutlined]))

(def ant-button (r/adapt-react-class AntButton))
(def ant-smile-outlined (r/adapt-react-class AntSmileOutlined))


(defn view []
  (let []
    [:section
     [:a {:href "/board"} "/board"]
     [:br]
     [:a {:href "/map"} "/map"]
     [:br]
     [ant-button {:value "button" } "button"]
     [:br]
     [ant-smile-outlined]
     #_[:button
      {:on-click (fn [] (rf/dispatch [:ui.alpha.evs/set-active-view :map-view]))} "map"]]))

