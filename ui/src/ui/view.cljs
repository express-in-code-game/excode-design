(ns ui.view
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require [cljs.repl :as repl]
            [cljs.pprint :as pp]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [shadow.loader :as loader]
            [ui.subs :as subs]
            [ui.home.view]
            [ui.map.view]
            [ui.board.view]
            [cljs.core.async :refer [<! timeout]]
            [ui.config :as config]
            [clojure.string :as string]
            [ui.routes :refer [set-path!]]))

(defn not-found-view
  [path]
  [:div (str path " not found")])

(defn- views [view-name]
  #_(prn view-name)
  (case view-name
    :home-view [ui.home.view/view]
    :map-view [ui.map.view/view]
    :board-view [ui.board.view/view]
    ; nil [:div "loading..."]
    [not-found-view view-name]))

(defn main-view []
  (let [active-view (rf/subscribe [::subs/active-view])]
    [views @active-view]))

(defn view
  []
  [main-view])