(ns app.main
  (:require
   [clojure.core.async :as async :refer [<! >!  chan go alt! take! put!  alts! pub sub]]
   [ui.ext.host]
   [ui.ext.router]
   [ui.ext.loader]
   [ui.ext.layout]))


(defonce routes ["/" {"" :ext-events
                      "events" :ext-events
                      "games" :ext-games
                      "user" :ext-user
                      "game/" {[:id ""] :ext-starnet}
                      "stats/" {[:id ""] :ext-stats}}])


(defonce modules {:shared {:entries []}
                  :app {:entries ['app.main]
                        :depends-on #{:shared}}
                  :ext-games {:entries ['ui.ext.games]
                              :init-fn #(resolve 'ui.ext.games/-main)}
                  :ext-starnet {:entries ['ui.ext.starnet]
                                :init-fn #(resolve 'ui.ext.starnet/-main)}
                  :ext-events {:entries ['ui.ext.events]
                               :init-fn #(resolve 'ui.ext.events/-main)}})

(defn ^:export -main
  []
  (println "app.main/-main")
  (ui.ext.host/-main)
  (ui.ext.router/-main)
  (ui.ext.loader/-main)
  (ui.ext.layout/-main))

(defonce _ (-main))


(comment

  
  ;;
  )

