(ns ui.home.view
  (:require  [reagent.core :as r]
             [cljs.repl :as repl]
             [cljs.pprint :as pp]
             [re-frame.core :as rf]
             [ui.home.subs :as subs]
             [ui.home.evs :as evs]
             ))



(defn view []
  (let []
    [:section
     [:a {:href "/map"} "/map"]
     #_[:button
      {:on-click (fn [] (rf/dispatch [:ui.evs/set-active-view :map-view]))} "map"]]))

