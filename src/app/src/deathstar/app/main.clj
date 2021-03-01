(ns deathstar.app.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [clojure.java.io :as io]

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

   [deathstar.data.spec :as data.spec]
   [deathstar.app.spec :as app.spec]

   [deathstar.app.tray]
   [deathstar.app.lacinia]
   [deathstar.app.docker]))

(declare start-dgraph)

(def channels (merge
               (let [ops| (chan 10)]
                 {::ops| ops|
                  ::exit| (chan 1)})))

(def ctx {::app.spec/state* (atom {})})

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::ops| ::exit|]} channels]
    (go
      (loop []
        (when-let [[value port] (alts! [ops| exit|])]
          (condp = port
            exit|
            (let []
              (println ::exit|)
              (<! (deathstar.app.docker/stop-dgraph))
              (println ::exiting)
              (System/exit 0))

            ops|
            (condp = (:op value)

              ::init
              (let [{:keys []} value]
                (println ::init)
                (<! (deathstar.app.docker/count-images))
                (<! (deathstar.app.docker/start-dgraph))
                (println ::init-done)))))
        (recur)))))

;; (def _ (create-proc-ops channels {})) ;; cuases native image to fail

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

(def app
  (reitit.http/ring-handler
   (reitit.http/router
    [["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "my-api"}}
             :handler (reitit.swagger/create-swagger-handler)}}]

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
                          :body (io/input-stream
                                 (io/resource "reitit.png"))})}}]]

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
                           (reitit.http.interceptors.multipart/multipart-interceptor)]}})
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
  []
  (let [port 3080]
    #_(jetty/run-jetty #'app {:port port :host "0.0.0.0" :join? false :async? true})
    (aleph.http/start-server (aleph.http/wrap-ring-async-handler #'app) {:port port :host "0.0.0.0"})
    (println (format "server running in port %d" port))))

(defn -main [& args]
  (println ::-main)
  (create-proc-ops channels {})
  (deathstar.app.tray/create {:deathstar.app.tray/exit| (::exit| channels)})
  (start)
  (deathstar.app.lacinia/start)
  (put! (::ops| channels) {:op ::init}))
