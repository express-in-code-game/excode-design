(ns ui.alpha.board.view
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [cljs.repl :as repl]
   [cljs.pprint :as pp]
   [goog.string :refer [format]]
   [ui.alpha.board.evs :as evs]
   [ui.alpha.board.subs :as subs]))

(defn view []
  (let []
    (fn []
      [:section
       [:a {:href "/home"} "/home"]
       [:a {:href "/map"} "/map"]
       #_[:button
        {:on-click (fn [] (rf/dispatch [:ui.alpha.evs/set-active-view :home-view]))} "home"]])))

(defn actions []
  [])
