(ns deathstar.app.dgraph
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [clojure.java.io :as io]
   [byte-streams]
   [aleph.http]))


(defn load-schema
  []
  (go
    (let [response
          (->
           @(aleph.http/post
             "http://localhost:8080/admin/schema"
             {:body (clojure.java.io/file (io/resource "schema.gql"))})
           :body
           byte-streams/to-string)]
      (println response))))