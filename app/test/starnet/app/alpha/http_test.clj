(ns starnet.app.alpha.http-test
  (:require
   [starnet.app.alpha.spec]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
   [starnet.app.alpha.http :as app-http]
   [io.pedestal.http :as http]
   [io.pedestal.test :as test :refer [response-for]]))

(def service (::http/service-fn (http/create-servlet app-http/service)))
  
(deftest route-tests
  (is (= (response-for service :get "/todo"))))