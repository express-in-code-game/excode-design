(ns deathstar.p2p.main
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
(defonce axios (.-default (js/require "axios")))
(defonce IpfsClient (js/require "ipfs-http-client"))
(defonce http (js/require "http"))
(defonce Url (js/require "url"))
(defonce express (js/require "express"))
(defonce cors (js/require "cors"))
(defonce bodyParser (js/require "body-parser"))

(def HTTP_PORT 8000)

(defonce app (express))
(defonce server (.createServer http app))

(.use app (cors))
(.use app (.text bodyParser #js {"type" "text/plain" #_"*/*"
                                 "limit" "100kb"}))

(.get app "/"
      (fn [request response next]
        (go
          (<! (timeout 2000))
          (.send response "hello world"))))


(comment

  (js/Object.keys ipfs)
  (js/Object.keys ipfs.pubsub)

  (go
    (let [id (<p! (daemon._ipfs.id))]
      (println (js-keys id))
      (println (.-id id))
      (println (format "id is %s" id))))

  ;;
  )

(declare)


(defn main [& args]
  (println ::main)
  (.listen server HTTP_PORT))

(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))
