(ns deathstar.app.lacinia
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [io.pedestal.http]
   [clojure.java.io]
   [com.walmartlabs.lacinia.pedestal2 :as lacinia.pedestal2]
   [com.walmartlabs.lacinia.schema :as lacinia.schema]
   [com.walmartlabs.lacinia.parser.schema :as lacinia.parser.schema]))

(def hello-schema
  (lacinia.schema/compile
   {:queries
    {:hello
      ;; String is quoted here; in EDN the quotation is not required
     {:type 'String
      :resolve (constantly "world")}}}))

;; Use default options:
(def service (lacinia.pedestal2/default-service hello-schema {:port 8888
                                                              :host "0.0.0.0"}))

;; This is an adapted service map, that can be started and stopped
;; From the REPL you can call server/start and server/stop on this service
(defonce runnable-service (io.pedestal.http/create-server service))

(defn start
  [channels]
  (go
    (println "starting lacinia")
    (io.pedestal.http/start runnable-service)))


(comment

  (lacinia.parser.schema/parse-schema
   (slurp (clojure.java.io/resource "schema.gql")))

  ;;
  )