(ns starnet.app.pad.buddy1
  (:require
   [clojure.repl :refer [doc]]
   [clj-time.core :as time]
   [buddy.auth.protocols :as proto]
   [buddy.auth.http :as http]
   [buddy.auth :refer [authenticated?]]
   [buddy.sign.jwt :as jwt]
   [buddy.core.hash :as hash]
   [buddy.core.keys :as keys]
   [buddy.sign.jws :as jws]
   [buddy.core.nonce :as nonce]
   [buddy.core.bytes :as bytes]
   [buddy.sign.compact :as cm]))


(comment

  ; https://funcool.github.io/buddy-sign/latest/

  (def data (jwt/sign {:userid 1} "secret"))

  (jwt/unsign data "secret")

  ;; Define claims with `:exp` key
  (def claims
    {:user 1 :exp (time/plus (time/now) (time/seconds 5))})

  ;; Serialize and sign a token with previously defined claims
  (def token (jwt/sign claims "key"))

  (jwt/unsign token "key")

  ;; use timestamp in the past
  (jwt/unsign token "key" {:now (time/minus (time/now) (time/seconds 55))})

  ;; Hash your secret key with sha256 for
  ;; create a byte array of 32 bytes because
  ;; is a requirement for default content
  ;; encryption algorithm
  (def secret (hash/sha256 "mysecret"))

  ;; Encrypt it using the previously
  ;; hashed key
  (def incoming-data (jwt/encrypt {:userid 1} secret {:alg :dir :enc :a128cbc-hs256}))

  (jwt/decrypt incoming-data secret)

  ;; Create keys instances
  (def ec-privkey (keys/private-key "resources/ecprivkey.pem"))
  (def ec-pubkey (keys/public-key "resources/ecpubkey.pem"))

  ;; Use them like plain secret password with hmac algorithms for sign
  (def signed-data (jwt/sign {:foo "bar"} ec-privkey {:alg :es256}))

  ;; And unsign
  (def unsigned-data (jwt/unsign signed-data ec-pubkey {:alg :es256}))


  ;; Create keys instances
  (def privkey (keys/private-key "resources/privkey.pem" "pass"))
  (def pubkey (keys/public-key "resources/pubkey.pem"))

  ;; Encrypt data
  (def encrypted-data (jwt/encrypt {:foo "bar"} pubkey
                                   {:alg :rsa-oaep
                                    :enc :a128cbc-hs256}))

  ;; Decrypted 
  (def decrypted-data (jwt/decrypt encrypted-data privkey
                                   {:alg :rsa-oaep
                                    :enc :a128cbc-hs256}))


  ;; JSON Web Signature (JWS)

  (def data (nonce/random-bytes 1024))
  (def message (jws/sign data "secret"))

  (bytes/equals? (jws/unsign message "secret") data)

  ;; JSON Web Encryption (JWE)

  (def key32 (nonce/random-bytes 32))
  (def data (nonce/random-bytes 1024))

  (def message (jws/sign data key32))
  (bytes/equals? (jws/unsign message key32) data)


  ;; Compact message signing

  (def data (cm/sign #{:foo :bar} "secret"))
  (cm/unsign data "secret")
  
  (cm/unsign data "secret" {:max-age (* 15 60)})


  ;;
  )