(ns starnet.ui.alpha.view
  (:use-macros
   [cljs.core.async.macros :only [go]])
  (:require
   [cljs.repl :as repl]
   [cljs.pprint :as pp]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [shadow.loader :as loader]
   [starnet.ui.alpha.subs :as subs]
   [starnet.ui.alpha.home.view]
   [starnet.ui.alpha.map.view]
   [starnet.ui.alpha.board.view]
   [cljs.core.async :refer [<! timeout]]
   [starnet.ui.alpha.config :as config]
   [clojure.string :as string]
   [starnet.ui.alpha.routes :refer [set-path!]]))

(defn not-found-view
  [path]
  [:div (str path " not found")])

(defn- views [view-name]
  #_(prn view-name)
  (case view-name
    :home-view [starnet.ui.alpha.home.view/view]
    :map-view [starnet.ui.alpha.map.view/view]
    :board-view [starnet.ui.alpha.board.view/view]
    ; nil [:div "loading..."]
    [not-found-view view-name]))

(defn main-view []
  (let [active-view (rf/subscribe [::subs/active-view])]
    [views @active-view]))

(defn view
  []
  [main-view])