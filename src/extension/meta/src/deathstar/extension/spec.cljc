(ns deathstar.extension.spec
  #?(:cljs (:require-macros [deathstar.extension.spec]))
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]

   [deathstar.multiplayer.spec :as multiplayer.spec]
   [deathstar.multiplayer.remote.spec :as remote.spec]))

(do (clojure.spec.alpha/check-asserts true))

(s/def ::deathstar-dir string?)
(s/def ::settings-filepath string?)
(s/def ::nrepl-port int?)
(s/def ::server-port int?)
(s/def ::server-host string?)
(s/def ::http-path string?)

(s/def ::settings (s/keys :req [::nrepl-port
                                ::server-port
                                ::multiplayer.spec/username]
                          :opt [::deathstar-dir]))

(s/def ::settings-filepaths (s/coll-of ::settings-filepath
                                       :kind vector? :distinct true :into [] :count nil))

(s/def ::state (s/keys ::req [::settings
                            ::settings-filepaths
                            ::remote.spec/status]))
