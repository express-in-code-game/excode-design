(ns app.api.http
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [io.pedestal.http.route :as route]
   [io.pedestal.http :as http]

   [app.api]))

(def ^:dynamic *proc* nil)

(defn respond-hello [request]
  {:status 200 :body "agents-identity"})

(def routes
  (route/expand-routes
   #{["/" :get respond-hello :route-name :home]
     ["/greet" :get respond-hello :route-name :greet]}))

(def host "0.0.0.0")
(def port 8080)

(defn create-server []
  (http/create-server
   {::http/routes routes
    ::http/type   :jetty
    ::http/host host
    ::http/port port}))

(defn start []
  (println (format "; stared http server on %s:%s" host port))
  (http/start (create-server)))