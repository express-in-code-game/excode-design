(ns ui.alpha.map.view
  (:require  [reagent.core :as r]
             [re-frame.core :as rf]
             [cljs.repl :as repl]
             [cljs.pprint :as pp]
             [goog.string :refer [format]]
             [ui.alpha.map.evs :as evs]
             [ui.alpha.map.subs :as subs]
             ))

(defn view []
  (let []
    (fn []
      [:section
       [:a {:href "/home"} "/home"]
       #_[:button
        {:on-click (fn [] (rf/dispatch [:ui.alpha.evs/set-active-view :home-view]))} "home"]])))

(defn actions []
  [])
