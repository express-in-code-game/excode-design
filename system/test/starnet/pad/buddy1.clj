(ns starnet.app.pad.buddy1
  (:require
   [clojure.repl :refer [doc]]
   [clj-time.core :as time]
   [buddy.auth.protocols :as proto]
   [buddy.auth.http :as http]
   [buddy.auth :refer [authenticated?]]
   [buddy.sign.jwt :as jwt]
   [buddy.sign.jwe :as jwe]
   [buddy.core.hash :as hash]
   [buddy.core.keys :as keys]
   [buddy.sign.jws :as jws]
   [buddy.core.nonce :as nonce]
   [buddy.core.bytes :as bytes]
   [buddy.hashers :as hashers]
   [buddy.sign.compact :as cm]
   [buddy.auth.backends :as backends]
   [buddy.auth.middleware :refer [wrap-authentication]]
   [cheshire.core :as json]))


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
  (def passphrase (slurp "resources/passphrase.tmp"))
  (def privkey (keys/private-key "resources/privkey.pem" passphrase))
  (def pubkey (keys/public-key "resources/pubkey.pem"))

  ;; Encrypt data
  (def encrypted-data (jwt/encrypt {:foo "bar"} pubkey
                                   {:alg :rsa-oaep
                                    :enc :a128cbc-hs256}))

  ;; Decrypted 
  (def decrypted-data (jwt/decrypt encrypted-data privkey
                                   {:alg :rsa-oaep
                                    :enc :a128cbc-hs256}))

  jwe/encrypt
  jwe/decrypt


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

(comment

  ; https://funcool.github.io/buddy-auth/latest/
  ; https://github.com/funcool/buddy-auth/tree/master/examples

  ;;;; Token

  ;; Define a in-memory relation between tokens and users:
  (def tokens {:2f904e245c1f5 :admin
               :45c1f5e3f05d0 :foouser})

  ;; Define an authfn, function with the responsibility
  ;; to authenticate the incoming token and return an
  ;; identity instance

  (defn my-authfn
    [request token]
    (let [token (keyword token)]
      (get tokens token nil)))

  ;; Create an instance
  (def backend (backends/token {:authfn my-authfn}))

  ;; The authfn should return something that will be associated to the :identity key in the request.

  (defn my-handler
    [request])

  ;; This is a possible aspect of the authorization header
  ;; Authorization: Token 45c1f5e3f05d0

  ;; Wrap the ring handler.
  (def app (-> my-handler
               (wrap-authentication backend)))

  ;;;; https://funcool.github.io/buddy-auth/latest/#signed-jwt
  ;;;; https://github.com/funcool/buddy-auth/tree/master/examples/jws

  (def secret "mysecret")
  (def backend (backends/jws {:secret secret}))

  ;; and wrap your ring application with
  ;; the authentication middleware

  (def app (-> 'your-ring-app
               (wrap-authentication backend)))


  ;; Now you should have a login endpoint in your ring application that will have the responsibility of generating valid tokens:
  ;; complete example ->
  (defn login-handler
    [request]
    (let [data (:form-params request)
          user (find-user (:username data)   ;; (implementation ommited)
                          (:password data))
          token (jwt/sign {:user (:id user)} secret)]
      {:status 200
       :body (json/encode {:token token})
       :headers {:content-type "application/json"}}))



  ;;;; https://funcool.github.io/buddy-auth/latest/#encrypted-jwt
  ;;;; https://github.com/funcool/buddy-auth/blob/master/examples/jwe/src/authexample/web.clj

  (def pubkey (keys/public-key "pubkey.pem"))
  (def privkey (keys/private-key "privkey.pem"))

  (def backend
    (backends/jwe {:secret privkey
                   :options {:alg :rsa-oaep
                             :enc :a128-hs256}}))

  ;; and wrap your ring application with
  ;; the authentication middleware

  (def app (-> your-ring-app
               (wrap-authentication backend)))

  ; ... login-handler same as JWS
  
  ; https://funcool.github.io/buddy-auth/latest/#authorization
  

  ;;
  )

(comment

  ; https://funcool.github.io/buddy-hashers/latest

  (def s "passwordpassw sword a1 ")

  (def h (hashers/derive s {:alg :bcrypt+sha512 :iterations 4}))
  (def h (hashers/derive s {:alg :pbkdf2+sha512 :iterations 50000}))
  (def h (hashers/derive s {:alg :pbkdf2+sha256 :iterations 50000}))

  (count h)
  
  (hashers/check s h)


  ;;
  )