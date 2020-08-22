(ns deathstar.gui.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]
   [reagent.core :as r]
   [reagent.dom :as rdom]

   [cljctools.vscode.tab-conn :as tab-conn.api]
   [deathstar.gui.ops :as ops.api]
   [deathstar.gui.render :as render.api]))

(def channels (merge
               (tab-conn.api/create-channels)
               (ops.api/create-channels)))

(def conn (tab-conn.api/create-proc-conn channels {}))

(def state (render.api/create-state))

(defn TMP-counter-inc
  []
  (swap! state update :counter inc))

(def app (app.api/create-proc-ops channels {:state state}))

(defn ^:export main
  []
  (println "deathstar.gui.main")
  (render.api/render-ui channels {:state state})
  #_(proc-main channels {:state state}))

(do (main))

