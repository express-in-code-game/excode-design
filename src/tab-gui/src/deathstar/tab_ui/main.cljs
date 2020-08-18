(ns deathstar.tabapp.solution-space.main
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
   [deathstar.tabapp.solution-space.app :as app.api]
   [deathstar.tabapp.solution-space.render :as render.api]
   ))

(def conn|| (tab-conn.api/create-channels))
(def app|| (app.api/create-channels))

(def conn (tab-conn.api/create-proc-conn conn|| {}))

(def state (render.api/create-state))

(def app (app.api/create-proc-ops (merge conn|| app||) {:state state
                                                        :conn conn}))

(defn ^:export main
  []
  (render.api/render-ui (merge conn|| app||) {:state state})
  #_(proc-main channels {:state state}))

