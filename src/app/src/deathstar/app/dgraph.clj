(ns deathstar.app.dgraph
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   #_[clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   
   [byte-streams]
   [aleph.http]
   [jsonista.core :as j]))


(defn load-schema
  []
  (go
    (let [response
          (->
           @(aleph.http/post
             "http://localhost:8080/admin/schema"
             {:body (clojure.java.io/file (clojure.java.io/resource "schema.gql"))})
           :body
           byte-streams/to-string)]
      (println response))))

(defn query-users
  []
  (go
    (let [response
          (->
           @(aleph.http/post
             "http://localhost:8080/graphql"
             {:body (j/write-value-as-string
                     {"query"  (slurp (clojure.java.io/resource "query-users.gql"))
                      "variables" {}})
              :headers {:content-type "application/json"}})
           :body
           byte-streams/to-string)]
      (println response))))

(defn add-random-user
  []
  (go
    (let [response
          (->
           @(aleph.http/post
             "http://localhost:8080/graphql"
             {:body (j/write-value-as-string
                     {"query"  (slurp (clojure.java.io/resource "add-user.gql"))
                      "variables" {"user" {"username" (gen/generate (s/gen string?))
                                           "name" (gen/generate (s/gen string?))
                                           "password" (gen/generate (s/gen string?))}}})
              :headers {:content-type "application/json"}})
           :body
           byte-streams/to-string)]
      (println response))))