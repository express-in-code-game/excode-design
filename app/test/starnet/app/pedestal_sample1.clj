(ns starnet.app.alpha.pedestal-sample1
  (:require
   [clojure.repl :refer [doc]]
   [io.pedestal.log :as log]
   [io.pedestal.http.route :as route]
   [io.pedestal.http.body-params :as body-params]
   [io.pedestal.http.route.definition :refer [defroutes]]
   [ring.util.response :as ring-resp]
   [clojure.core.async :as async]
   [io.pedestal.http.jetty.websockets :as ws]
   [io.pedestal.http :as http]
   [io.pedestal.http :as server]
   [io.pedestal.test :as test])
  (:import
   [org.eclipse.jetty.websocket.api Session]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))
(def accepted (partial response 202))

(def echo
  {:name :echo
   :enter
   (fn [context]
     (let [request (:request context)
           response (ok request)]
       (assoc context :response response)))})

(defonce database (atom {}))

(def db-interceptor
  {:name :database-interceptor
   :enter
   (fn [context]
     (update context :request assoc :database @database))
   :leave
   (fn [context]
     (if-let [[op & args] (:tx-data context)]
       (do
         (apply swap! database op args)
         (assoc-in context [:request :database] @database))
       context))})

(defn make-list [nm]
  {:name  nm
   :items {}})

(defn make-list-item [nm]
  {:name  nm
   :done? false})

(def list-create
  {:name :list-create
   :enter
   (fn [context]
     (let [nm       (get-in context [:request :query-params :name] "Unnamed List")
           new-list (make-list nm)
           db-id    (str (gensym "l"))
           url      (route/url-for :list-view :params {:list-id db-id})]
       (assoc context
              :response (created new-list "Location" url)
              :tx-data [assoc db-id new-list])))})

(defn find-list-by-id [dbval db-id]
  (get dbval db-id))

(def list-view
  {:name :list-view
   :enter
   (fn [context]
     (if-let [db-id (get-in context [:request :path-params :list-id])]
       (if-let [the-list (find-list-by-id (get-in context [:request :database]) db-id)]
         (assoc context :result the-list)
         context)
       context))})

(def entity-render
  {:name :entity-render
   :leave
   (fn [context]
     (if-let [item (:result context)]
       (assoc context :response (ok item))
       context))})

(defn find-list-item-by-ids [dbval list-id item-id]
  (get-in dbval [list-id :items item-id] nil))

(def list-item-view
  {:name :list-item-view
   :leave
   (fn [context]
     (if-let [list-id (get-in context [:request :path-params :list-id])]
       (if-let [item-id (get-in context [:request :path-params :item-id])]
         (if-let [item (find-list-item-by-ids (get-in context [:request :database]) list-id item-id)]
           (assoc context :result item)
           context)
         context)
       context))})

(defn list-item-add
  [dbval list-id item-id new-item]
  (if (contains? dbval list-id)
    (assoc-in dbval [list-id :items item-id] new-item)
    dbval))

(def list-item-create
  {:name :list-item-create
   :enter
   (fn [context]
     (if-let [list-id (get-in context [:request :path-params :list-id])]
       (let [nm       (get-in context [:request :query-params :name] "Unnamed Item")
             new-item (make-list-item nm)
             item-id  (str (gensym "i"))]
         (-> context
             (assoc :tx-data  [list-item-add list-id item-id new-item])
             (assoc-in [:request :path-params :item-id] item-id)))
       context))})

(def routes
  (route/expand-routes
   #{["/todo"                    :post   [db-interceptor list-create]]
     ["/todo"                    :get    echo :route-name :list-query-form]
     ["/todo/:list-id"           :get    [entity-render db-interceptor list-view]]
     ["/todo/:list-id"           :post   [entity-render list-item-view db-interceptor list-item-create]]
     ["/todo/:list-id/:item-id"  :get    [entity-render list-item-view db-interceptor]]
     ["/todo/:list-id/:item-id"  :put    echo :route-name :list-item-update]
     ["/todo/:list-id/:item-id"  :delete echo :route-name :list-item-delete]}))



#_(defroutes routes
  ;; Defines "/" and "/about" routes with their associated :get handlers.
  ;; The interceptors defined after the verb map (e.g., {:get home-page}
  ;; apply to / and its children (/about).
    [[["/" {:get home-page}
       ^:interceptors [(body-params/body-params) http/html-body]
       ["/about" {:get about-page}]]]])

(comment



  ;;
  )

(def ws-clients (atom {}))

(defn new-ws-client
  [ws-session send-ch]
  (async/put! send-ch "This will be a text message")
  (swap! ws-clients assoc ws-session send-ch))

;; This is just for demo purposes
(defn send-and-close! []
  (let [[ws-session send-ch] (first @ws-clients)]
    (async/put! send-ch "A message from the server")
    ;; And now let's close it down...
    (async/close! send-ch)
    ;; And now clean up
    (swap! ws-clients dissoc ws-session)))

;; Also for demo purposes...
(defn send-message-to-all!
  [message]
  (doseq [[^Session session channel] @ws-clients]
    ;; The Pedestal Websocket API performs all defensive checks before sending,
    ;;  like `.isOpen`, but this example shows you can make calls directly on
    ;;  on the Session object if you need to
    (when (.isOpen session)
      (async/put! channel message))))

(def ws-paths
  {"/ws" {:on-connect (ws/start-ws-connection new-ws-client)
          :on-text (fn [msg] (log/info :msg (str "A client sent - " msg)))
          :on-binary (fn [payload offset length] (log/info :msg "Binary Message!" :bytes payload))
          :on-error (fn [t] (log/error :msg "WS Error happened" :exception t))
          :on-close (fn [num-code reason-text]
                      (log/info :msg "WS Closed:" :reason reason-text))}})

(def port 8080)
(def port-ssl 8443)
(def host "0.0.0.0")

;; Consumed by jetty-web-sockets.server/create-server
;; See http/default-interceptors for additional options you can configure
;; http://pedestal.io/reference/service-map
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]
              ;; ::http/allowed-origins ["*"]

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::http/type :jetty
              ;; http://pedestal.io/reference/jetty
              ::http/container-options {:context-configurator #(ws/add-ws-endpoints % ws-paths)
                                        ; :h2c? true
                                        ; :h2? true
                                        :ssl? true
                                        :ssl-port port-ssl
                                        :keystore "resources/keystore.jks"
                                        :key-password "keystore"}
              ::http/host host
              ::http/port port})

(defn -main-dev
  [& args]
  (println (str "; starting http server on " host ":" port))
  (when (get-in service [::http/container-options :ssl-port])
    (println (str "; starting https server on " host ":" port-ssl)))
  (-> service ;; start with production configuration
      (merge {:env :dev
              ;; do not block thread that starts web server
              ::server/join? false
              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ::server/routes #(deref #'routes)
              ;; all origins are allowed in dev mode
              ::server/allowed-origins {:creds true :allowed-origins (constantly true)}})
      ;; Wire up interceptor chains
      server/default-interceptors
      server/dev-interceptors
      server/create-server
      server/start))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  ;; This is an adapted service map, that can be started and stopped
  ;; From the REPL you can call server/start and server/stop on this service
  (defonce runnable-service (server/create-server service))
  (println "\nCreating your server...")
  (server/start runnable-service))