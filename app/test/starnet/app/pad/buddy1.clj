(ns starnet.app.pad.buddy1
  (:require
   [clojure.repl :refer [doc]]
   [clj-time.core :as time]
   [buddy.auth.protocols :as proto]
   [buddy.auth.http :as http]
   [buddy.auth :refer [authenticated?]]
   [buddy.sign.jwt :as jwt]
   [buddy.core.hash :as hash]))


(comment

  (def data (jwt/sign {:userid 1} "secret"))

  (jwt/unsign data "secret")

  (def claims
    {:user 1 :exp (time/plus (time/now) (time/seconds 5))})

  (def token (jwt/sign claims "key"))
  
  (jwt/unsign token "key")
  
  (jwt/unsign token "key" {:now (time/minus (time/now) (time/seconds 55))})
  
  (def secret (hash/sha256 "mysecret"))

  (def incoming-data (jwt/encrypt {:userid 1} secret {:alg :dir :enc :a128cbc-hs256}))
  
  (jwt/decrypt incoming-data secret)
  
  
  
  ;;
  )