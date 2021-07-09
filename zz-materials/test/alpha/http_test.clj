(ns starnet.alpha.http-test
  (:require
   [starnet.alpha.spec]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
   [starnet.alpha.http :as app-http]
   [io.pedestal.http :as http]
   [io.pedestal.test :as test :refer [response-for]]))

(def service (::http/service-fn (http/create-servlet (app-http/service-config 'channels (app-http/routes)))))
  
(deftest route-tests
  (is (= (response-for service :get "/user")))
  (is (= (response-for service :post "/user"
                       :body (str (gen/generate (s/gen :u/user)))
                       :headers {"Content-Type" "application/edn"}))))