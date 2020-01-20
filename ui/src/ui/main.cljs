(ns ui.main
  (:require [cljs.repl :as repl]
            [cljs.pprint :as pp]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [re-pressed.core :as rp]
            [ui.evs :as evs]
            [ui.routes :as routes]
            [ui.config :as config]
            [ui.subs :as subs]
            [ui.view :refer [view]]
            [devtools.core :as devtools]))

(devtools/install!)
#_(enable-console-print!)

(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [view]
            (.getElementById js/document "app")))

(defn dev-setup []
  (when config/debug?
    (js/document.addEventListener "keyup" (fn [e]
                                            (when (= (.. e -key) "r")
                                              (mount-root)
                                              (prn "r/render"))))
    #_(println "dev mode" config/debug?)))

(defn ^:export main []
  (routes/app-routes)
  (rf/dispatch-sync [::evs/initialize-db])
  (rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (dev-setup)
  (mount-root))

(defn ^:dev/after-load after-load []
  #_(js/console.log "--after load")
  (mount-root))
