(ns cljctools.puppeteer.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]))

(def fs (js/require "fs"))
(def path (js/require "path"))
(def puppeteer (js/require "puppeteer"))


(def ^:dynamic browser nil)

(comment

  (.wsEndpoint browser)


  ;;
  )

(defn main []
  (println ::main)
  (go
    (try
      (set! browser (<p! (.launch puppeteer (clj->js {"args" ["--remote-debugging-port=9222"
                                                              "--remote-debugging-address=0.0.0.0"
                                                              "--no-sandbox"]}))))
      #_(let [page (<p! (.newPage browser))]
          (<p! (.goto page "https://news.ycombinator.com" #js {"waitUntil" "networkidle2"}))
          (<p! (.pdf page #js {"path" "hn.pdf"
                               "format" "A4"})))
      #_(<p! (.close browser))
      (catch js/Error err (println err)))))

(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))