(ns deathstar.desktop.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.string]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]))

(defonce fs (js/require "fs"))
(defonce path (js/require "path"))
(defonce Electron (js/require "electron"))
(def ElectronApp (.-app Electron))
(def ElectronBrowserWindow (.-BrowserWindow Electron))

(declare)

(defn main [& args]
  (println ::main)
  (go
    (let [_ (<p! (.whenReady ElectronApp))
          create-window
          (fn []
            (let [main-window (ElectronBrowserWindow.
                               (clj->js {"width" 800
                                         "height" 600
                                         "icon" (.join path
                                                       js/__dirname
                                                       "../../../../"
                                                       "logo"
                                                       "svg"
                                                       "logo_bottom_right-colors-green-4-728-square.png")
                                         "webPreferences" {}}))]
              (.loadFile main-window (.join path js/__dirname "../../../ui/resources/public/index.html"))))]
      (create-window)
      (.on ElectronApp "activate"
           (fn []
             (when (empty? (.getAllWindows ElectronBrowserWindow))
               (create-window))))
      (.on ElectronApp "window-all-closed"
           (fn []
             (when (not= js/global.process.platform "darwin")
               (.quit ElectronApp)))))))

(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))
