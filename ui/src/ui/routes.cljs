(ns ui.routes
  (:require [clojure.repl]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            #_[goog.events]
            [reagent.core :as r]
            [re-frame.core :as rf])
  (:import goog.History
           goog.history.Html5History
           goog.history.Html5History.TokenTransformer
           goog.history.EventType
           goog.Uri))

#_(def routes [".+" :map-view])

(def routes ["/" {""      :home-view
                  "home" :home-view
                  "board" :board-view
                  "map" :map-view}])


#_(def _ (events/listen history EventType.NAVIGATE
                      (fn [e]
                        (when-let [match (-> (.-token e) match-fn identity-fn)]
                          (dispatch-fn match)))))

(defn- parse-url [url]
  (merge
   {:url url}
   (bidi/match-route routes url)))

(defn- dispatch-route [matched-route]
  (prn matched-route)
  (let [handler (:handler matched-route)
        url (:url matched-route)]
    (rf/dispatch [:ui.evs/set-active-view handler])))

(declare history)

(defn app-routes []
  (defonce history (pushy/pushy dispatch-route parse-url))
  (pushy/start! history))

;Cannot infer target type in expression
;https://clojurescript.org/guides/externs#externs-inference
(defn ^:export set-path!
  [path]
  (.setToken ^js/Object (.-history history)  path))

(def path-for (partial bidi/path-for routes))

#_(ui.routes/set-path! (str "/" (panel->module-name (keyword key))))