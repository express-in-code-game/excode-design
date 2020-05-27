(ns ui.ext.games.render
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]))


(defn render
  []
  (rdom/render [:div "ui.ext.games.render"]  (.getElementById js/document "content")))