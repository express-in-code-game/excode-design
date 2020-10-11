(ns deathstar.server.spec
  #?(:cljs (:require-macros [deathstar.server.spec]))
  (:require
   [clojure.spec.alpha :as s]))


(s/def ::host string?)
(s/def ::port int?)
(s/def ::nrepl-port int?)
(s/def ::http-chan-path string?)


(s/def ::server (s/keys :req [::host
                              ::port
                              ::nrepl-port
                              ::http-chan-path]))