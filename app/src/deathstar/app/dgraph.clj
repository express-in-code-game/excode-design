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
   [jsonista.core :as j]
   [deathstar.spec]))


(def base-url "http://localhost:3088")

(defn load-schema
  []
  (go
    (let [response
          (->
           @(aleph.http/post
             (str base-url "/admin/schema")
             {:body (clojure.java.io/file (clojure.java.io/resource "dgraph/schema.gql"))})
           :body
           byte-streams/to-string)]
      (println response))))

(defn query-users
  []
  (go
    (let [response
          (->
           @(aleph.http/post
             (str base-url "/graphql")
             {:body (j/write-value-as-string
                     {"query"  (slurp (clojure.java.io/resource "dgraph/query-users.gql"))
                      "variables" {}})
              :headers {:content-type "application/json"}})
           :body
           byte-streams/to-string)]
      (println response))))

(defn query-user
  [{:keys [:deathstar.spec/username] :as opts}]
  (go
    (let [response
          (->
           @(aleph.http/post
             (str base-url "/graphql")
             {:body (j/write-value-as-string
                     {"query"  "
                                 queryUser () {
                                  username
                                 }
                                "
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
             (str base-url "/graphql")
             {:body (j/write-value-as-string
                     {"query"  (slurp (clojure.java.io/resource "dgraph/add-user.gql"))
                      "variables" {"user" {"username" (gen/generate (s/gen string?))
                                           "name" (gen/generate (s/gen string?))
                                           "password" (gen/generate (s/gen string?))}}})
              :headers {:content-type "application/json"}})
           :body
           byte-streams/to-string)]
      (println response))))