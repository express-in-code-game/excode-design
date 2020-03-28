(ns starnet.ui.alpha.worker
  (:require
   [clojure.repl :refer [doc]]
   [reagent.core :as r]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]

   [starnet.common.alpha.game.data]))


(aset js/self "onconnect" (fn [e]
                            (js/console.log "onconnect")
                            (let [port (aget (.-ports e) 0)]
                              (aset port "onmessage"
                                    (fn [e]
                                      (.postMessage port "123"))))))


(aset js/self "onmessage" (fn [e]
                            (js/console.log "msg from main script")
                            (.postMessage js/self "abc")
                            ))

(comment
  
  ;;
  )