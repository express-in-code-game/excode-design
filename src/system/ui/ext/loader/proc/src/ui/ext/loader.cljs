(ns ui.ext.loader
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]

   [ui.ext.router.client :as router.client]
   [ui.ext.host.client :as host.client]
   [ui.ext.loader.chan :as loader.chan]
   [cljs.loader :as loader]))

; https://github.com/clojure/clojurescript/blob/master/src/main/cljs/cljs/loader.cljs
; https://clojurescript.org/news/2017-07-10-code-splitting#_cljs_loader
; https://shadow-cljs.github.io/docs/UsersGuide.html#CodeSplitting


(def host (host.client/create))

(def router (router.client/create))


(defn proc-ops
  []
  (let [{:keys [routes|m]} router
        routes| (tap routes|m (chan 10))]
    (go (loop []
          (when-let [{:keys [url handler]} (<! routes|)]
            (when-let [module (get (deref (resolve 'app.main/modules)) handler)]
              (when-not (loader/loaded? handler)
                (loader/load handler
                             (fn [e]
                               (((:init-fn module)))))
                (loader/set-loaded! handler))))
          (recur)))
    (with-meta
      {}
      {})))

(defn -main
  []
  (println "ui.ext.loader/-main")
  (let [ext (proc-ops)]
    (host.client/register host 'ui.ext.loader ext)))