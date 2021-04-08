(ns tiefighter.peer.cors-interceptor
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.spec.alpha :as s]
   [ring.middleware.cors :as cors]
   [clojure.java.io]))

; by @kennyjwilli from this issue:
; https://github.com/metosin/reitit/issues/236

; should be a PR and merged into reitit

(s/def ::allow-origin string?)
(s/def ::allow-methods (s/coll-of keyword? :kind set?))
(s/def ::allow-credentials boolean?)
(s/def ::allow-headers (s/coll-of string? :kind set?))
(s/def ::expose-headers (s/coll-of string? :kind set?))
(s/def ::max-age nat-int?)
(s/def ::access-control
  (s/keys :opt-un [::allow-origin
                   ::allow-methods
                   ::allow-credentials
                   ::allow-headers
                   ::expose-headers
                   ::max-age]))

(s/def ::cors-interceptor
  (s/keys :opt-un [::access-control]))

(defn cors-interceptor
  []
  {:name    ::cors
   :spec    ::access-control
   :compile (fn [{:keys [access-control]} _]
              (when access-control
                (let [access-control (cors/normalize-config (mapcat identity access-control))]
                  {:enter (fn cors-interceptor-enter
                            [ctx]
                            (let [request (:request ctx)]
                              (if (or (and (cors/preflight? request)
                                           (cors/allow-request? request access-control)))
                                (let [resp (cors/add-access-control
                                            request
                                            access-control
                                            cors/preflight-complete-response)]
                                  (assoc ctx
                                         :response resp
                                         :queue nil))
                                ctx)))
                   :leave (fn cors-interceptor-leave
                            [ctx]
                            (let [request (:request ctx)]
                              (if (and (cors/origin request)
                                       (cors/allow-request? request access-control))
                                (if-let [response (:response ctx)]
                                  (assoc ctx
                                         :response
                                         (cors/add-access-control
                                          request
                                          access-control
                                          response)))
                                ctx)))})))})