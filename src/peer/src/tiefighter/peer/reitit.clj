(ns tiefighter.peer.reitit
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.spec.alpha :as s]
   [clojure.java.io]

   [clojure.spec.gen.alpha :as sgen]
   #_[clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]

   ;; reitit
   [reitit.http]
   [reitit.ring]
   [sieppari.async.core-async] ;; needed for core.async
   [sieppari.async.manifold]   ;; needed for manifold
   [muuntaja.interceptor]
   [reitit.coercion.spec]
   [reitit.swagger]
   [reitit.swagger-ui]
   [reitit.dev.pretty]
   [reitit.interceptor.sieppari]
   [reitit.http.coercion]
   [reitit.http.interceptors.parameters]
   [reitit.http.interceptors.muuntaja]
   [reitit.http.interceptors.exception]
   [reitit.http.interceptors.multipart]
   [ring.util.response]
   #_[ring.middleware.cors :refer [wrap-cors]]
            ;; Uncomment to use
            ; [reitit.ring.middleware.dev :as dev]
            ; [reitit.ring.spec :as spec]
            ; [spec-tools.spell :as spell]
   [ring.adapter.jetty]
   [aleph.http]
   [muuntaja.core]
   [spec-tools.core]
   [manifold.deferred :as d]

   ;;
   [tiefighter.peer.cors-interceptor]
   [tiefighter.spec]))

(defonce ^:private registry-ref (atom {}))

(s/def ::file reitit.http.interceptors.multipart/temp-file-part)
(s/def ::file-params (s/keys :req-un [::file]))

(s/def ::name string?)
(s/def ::size int?)
(s/def ::file-response (s/keys :req-un [::name ::size]))

(s/def ::x int?)
(s/def ::y int?)
(s/def ::total int?)
(s/def ::math-request (s/keys :req-un [::x ::y]))
(s/def ::math-response (s/keys :req-un [::total]))

(s/def ::seed string?)
(s/def ::results
  (spec-tools.core/spec
   {:spec (s/and int? #(< 0 % 100))
    :description "between 1-100"
    :swagger/default 10
    :reason "invalid number"}))

(defn interceptor [f x]
  {:enter (fn [ctx] (f (update-in ctx [:request :via] (fnil conj []) {:enter x})))
   :leave (fn [ctx] (f (update-in ctx [:response :body] conj {:leave x})))})

(defn handler [f]
  (fn [{:keys [via]}]
    (f {:status 200
        :body (conj via :handler)})))

(def <async> #(go %))
(def <deferred> d/success-deferred)


(defn app
  [channels]
  (reitit.http/ring-handler
   (reitit.http/router
    [["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "my-api"}}
             :handler (reitit.swagger/create-swagger-handler)}}]

     ["/sign-up"
      {:post {:summary "user sign-up"
              :parameters {:body (s/keys :req [:tiefighter.spec/username
                                               :tiefighter.spec/password])}
              :responses {200 {:body :tiefighter.spec/user-info}}
              :handler (fn [{{{:keys [:tiefighter.spec/username
                                      :tiefighter.spec/password]} :body} :parameters}]
                         (go
                           (<! (timeout 1000))
                           {:status 200
                            :body {:tiefighter.spec/username "hello"}}))}}]

     ["/sign-in"
      {:post {:summary "user sign-in"
              :parameters {:body (s/keys :req [:tiefighter.spec/username
                                               :tiefighter.spec/password])}
              :responses {200 {:body :tiefighter.spec/user-info}}
              :handler (fn [{{{:as body
                               :keys [:tiefighter.spec/username
                                      :tiefighter.spec/password]} :body} :parameters}]
                         (go
                           (<! (timeout 1000))
                           (let [token  nil
                                 user nil
                                 passord-valid? (and user true)]
                             (cond
                               (nil? user)
                               {:status 404
                                :body {:error (format "User %s not found" username)}}

                               (not passord-valid?)
                               {:status 401
                                :body {:error (format "Invlaid password")}}

                               passord-valid?
                               {:status 200
                                :headers {"Authorization" (format "Token %s" token)}
                                :body (select-keys body [:tiefighter.spec/username])}))))}}]

     ["/sign-out"
      {:post {:summary "user sign-out"
              :parameters {:body (s/keys :req [:tiefighter.spec/username
                                               :tiefighter.spec/password])}
              :responses {200 {:body :tiefighter.spec/user-info}}
              :handler (fn [{{{:keys [:tiefighter.spec/username
                                      :tiefighter.spec/password]} :body} :parameters}]
                         (go
                           (<! (timeout 1000))
                           {:status 200
                            :body {:tiefighter.spec/username "hello"}}))}}]

     ["/user-info"
      {:post {:summary "get information about user by username"
              :parameters {:body (s/keys :req [:tiefighter.spec/username])}
              :responses {200 {:body :tiefighter.spec/user-info}}
              :handler (fn [{{{:keys [:tiefighter.spec/username]} :body} :parameters}]
                         (go
                           (<! (timeout 1000))
                           {:status 200
                            :body {:tiefighter.spec/username "hello"}}))}}]

     ["/files"
      {:swagger {:tags ["files"]}}

      ["/upload"
       {:post {:summary "upload a file"
               :parameters {:multipart ::file-params}
               :responses {200 {:body ::file-response}}
               :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                          {:status 200
                           :body {:name (:filename file)
                                  :size (:size file)}})}}]

      ["/download"
       {:get {:summary "downloads a file"
              :swagger {:produces ["image/png"]}
              :handler (fn [_]
                         {:status 200
                          :headers {"Content-Type" "image/png"}
                          :body (clojure.java.io/input-stream
                                 (clojure.java.io/resource "reitit.png"))})}}]]

     ["/random-user"
      {:get {:swagger {:tags ["random-user"]}
             :summary "fetches random users asynchronously over the internet"
             :parameters {:query (s/keys :req-un [::results] :opt-un [::seed])}
             :responses {200 {:body any?}}
             :handler (fn [{{{:keys [seed results]} :query} :parameters}]
                        (go
                          (<! (timeout 1000))
                          @(d/chain
                            (aleph.http/get
                             "https://randomuser.me/api/"
                             {:query-params {:seed seed, :results results}})
                            :body
                            (partial muuntaja.core/decode "application/json")
                            :results
                            (fn [results]
                              {:status 200
                               :body results}))))}}]

     ["/async2"
      {:interceptors [(interceptor <async> :async)]
       :get {:swagger {:tags ["async"]}
             :interceptors [(interceptor <async> :get)]
             :handler (fn [request]
                        (go
                          (<! (timeout 1000))
                          {:status 200
                           :body [:async]}))}}]

     ["/async"
      {:interceptors [(interceptor <async> :async)]
       :get {:interceptors [(interceptor <async> :get)]
             :handler (handler <async>)}}]

     ["/deferred"
      {:interceptors [(interceptor <deferred> :deferred)]
       :get {:swagger {:tags ["deferred"]}
             :interceptors [(interceptor <deferred> :get)]
             :handler (handler <deferred>)}}]

     ["/math"
      {:swagger {:tags ["math"]}}

      ["/plus"
       {:get {:summary "plus with data-spec query parameters"
              :parameters {:query {:x int?, :y int?}}
              :responses {200 {:body {:total pos-int?}}}
              :handler (fn [{{{:keys [x y]} :query} :parameters}]
                         (go
                           (<! (timeout 1000))
                           {:status 200
                            :body {:total (+ x y)}}))}
        :post {:summary "plus with data-spec body parameters"
               :parameters {:body {:x int?, :y int?}}
               :responses {200 {:body {:total int?}}}
               :handler (fn [{{{:keys [x y]} :body} :parameters}]
                          {:status 200
                           :body {:total (+ x y)}})}}]

      ["/minus"
       {:get {:summary "minus with clojure.spec query parameters"
              :parameters {:query (s/keys :req-un [::x ::y])}
              :responses {200 {:body (s/keys :req-un [::total])}}
              :handler (fn [{{{:keys [x y]} :query} :parameters}]
                         {:status 200
                          :body {:total (- x y)}})}
        :post {:summary "minus with clojure.spec body parameters"
               :parameters {:body (s/keys :req-un [::x ::y])}
               :responses {200 {:body (s/keys :req-un [::total])}}
               :handler (fn [{{{:keys [x y]} :body} :parameters}]
                          {:status 200
                           :body {:total (- x y)}})}}]]]

    {;;:reitit.middleware/transform dev/print-request-diffs ;; pretty diffs
       ;;:validate spec/validate ;; enable spec validation for route data
       ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
     :exception reitit.dev.pretty/exception
     :data {:coercion reitit.coercion.spec/coercion
            :access-control {:access-control-allow-origin [#".*"]
                             :access-control-allow-methods #{:get :put :post :delete}}
            :muuntaja muuntaja.core/instance
            :interceptors [;; swagger feature
                           reitit.swagger/swagger-feature
                             ;; query-params & form-params
                           (reitit.http.interceptors.parameters/parameters-interceptor)
                             ;; content-negotiation
                           (reitit.http.interceptors.muuntaja/format-negotiate-interceptor)
                             ;; encoding response body
                           (reitit.http.interceptors.muuntaja/format-response-interceptor)
                             ;; exception handling
                           (reitit.http.interceptors.exception/exception-interceptor)
                             ;; decoding request body
                           (reitit.http.interceptors.muuntaja/format-request-interceptor)
                             ;; coercing response bodys
                           (reitit.http.coercion/coerce-response-interceptor)
                             ;; coercing request parameters
                           (reitit.http.coercion/coerce-request-interceptor)
                             ;; multipart
                           (reitit.http.interceptors.multipart/multipart-interceptor)
                             ;; cors
                           (tiefighter.peer.cors-interceptor/cors-interceptor)]}})
   (reitit.ring/routes
    (reitit.swagger-ui/create-swagger-ui-handler
     {:path "/swagger-ui"
      :config {:validatorUrl nil
               :operationsSorter "alpha"}})
    (reitit.ring/redirect-trailing-slash-handler #_{:method :add})
    (fn handle-index
      ([request]
       (when (= (:uri request) "/")
         (->
          (ring.util.response/resource-response "index.html" {:root "public"})
          (ring.util.response/content-type "text/html"))))
      ([request respond raise]
       (respond (handle-index request))))
    (reitit.ring/create-resource-handler {:path "/"
                                          :root "public"
                                          :index-files ["index.html"]})
    (reitit.ring/create-default-handler))
   {:executor reitit.interceptor.sieppari/executor}))

(defn start
  [channels {:keys [::port] :or {port 3080} :as opts}]
  (go
    (when-not (get @registry-ref port)
      (let [server (aleph.http/start-server
                    (aleph.http/wrap-ring-async-handler (app channels)  #_#'app)
                    {:port port :host "0.0.0.0"})]
        #_(jetty/run-jetty #'app {:port port :host "0.0.0.0" :join? false :async? true})
        (swap! registry-ref assoc port server)
        (println (format "started server on port %d" port))))))

(defn stop
  [channels {:keys [::port] :or {port 3080} :as opts}]
  (go
    (let [server (get @registry-ref port)]
      (when server
        (.close server)
        (swap! registry-ref dissoc port)
        (println (format "stopped server on port %d" port))))))

(defn app-static
  []
  (reitit.http/ring-handler
   (reitit.http/router
    []
    {;;:reitit.middleware/transform dev/print-request-diffs ;; pretty diffs
       ;;:validate spec/validate ;; enable spec validation for route data
       ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
     :exception reitit.dev.pretty/exception
     :data {:coercion reitit.coercion.spec/coercion
            :access-control {:access-control-allow-origin [#".*"]
                             :access-control-allow-methods #{:get :put :post :delete}}
            :muuntaja muuntaja.core/instance
            :interceptors [;; swagger feature
                           reitit.swagger/swagger-feature
                             ;; query-params & form-params
                           (reitit.http.interceptors.parameters/parameters-interceptor)
                             ;; content-negotiation
                           (reitit.http.interceptors.muuntaja/format-negotiate-interceptor)
                             ;; encoding response body
                           (reitit.http.interceptors.muuntaja/format-response-interceptor)
                             ;; exception handling
                           (reitit.http.interceptors.exception/exception-interceptor)
                             ;; decoding request body
                           (reitit.http.interceptors.muuntaja/format-request-interceptor)
                             ;; coercing response bodys
                           (reitit.http.coercion/coerce-response-interceptor)
                             ;; coercing request parameters
                           (reitit.http.coercion/coerce-request-interceptor)
                             ;; multipart
                           (reitit.http.interceptors.multipart/multipart-interceptor)
                             ;; cors
                           (tiefighter.peer.cors-interceptor/cors-interceptor)]}})
   (reitit.ring/routes
    (reitit.ring/redirect-trailing-slash-handler #_{:method :add})
    (fn handle-index
      ([request]
       (when (= (:uri request) "/")
         (->
          (ring.util.response/resource-response "index.html" {:root "public"})
          (ring.util.response/content-type "text/html"))))
      ([request respond raise]
       (respond (handle-index request))))
    (reitit.ring/create-resource-handler {:path "/"
                                          :root "public"
                                          :index-files ["index.html"]})
    (reitit.ring/create-default-handler))
   {:executor reitit.interceptor.sieppari/executor}))

(defn start-static
  [{:keys [::port] :or {port 3081} :as opts}]
  (go
    (when-not (get @registry-ref port)
      (let [server
            (aleph.http/start-server (aleph.http/wrap-ring-async-handler
                                      (app-static)  #_#'app)
                                     {:port port :host "0.0.0.0"})]
        (swap! registry-ref assoc port server)
        (println (format "server running in port %d" port))))))

(defn stop-static
  [{:keys [::port] :or {port 3081} :as opts}]
  (go
    (let [server (get @registry-ref port)]
      (when server
        (.close server)
        (swap! registry-ref dissoc port)
        (println (format "stopped server on port %d" port))))))