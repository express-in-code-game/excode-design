(ns starnet.common.pad.cljs-http1
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [cljs-http.client :as http]

   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop])
  (:import [goog.net XhrIo EventType WebSocket]
           [goog Uri]
           goog.history.Html5History))
  
  
(comment

  ; https://github.com/r0man/cljs-http

  (go (let [response (<! (http/get "https://api.github.com/users"
                                   {:with-credentials? false
                                    :query-params {"since" 135}}))]
        (prn (:status response))
        (prn (map :login (:body response)))))

  (http/get "http://example.com" {:channel (chan 1 (map :body))})

  (http/get "http://example.com" {:with-credentials? false
                                  :oauth-token "SecretBearerToken"})

  (http/post "https://example.com"
             {:with-credentials? false
              :headers {"Authorization" "SuperSecretToken"}})

  (let [progress-channel (async/chan)]
    (http/post "http://example.com" {:multipart-params [["key1" "value1"] ["my-file" my-file]] :progress progress-chan}))

  '{:directon dir :loaded uploaded_or_downloaded :total size}

  ;;
  )