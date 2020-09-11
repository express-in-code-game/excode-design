(ns deathstar.extension.spec
  #?(:cljs (:require-macros [deathstar.extension.spec]))
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]

   [deathstar.user.spec :as user.spec]
   [deathstar.hub.tap.remote.spec :as tap.remote.spec]))

(do (clojure.spec.alpha/check-asserts true))

(s/def ::deathstar-dir string?)
(s/def ::filepath string?)
(s/def ::nrepl-port int?)
(s/def ::server-port int?)
(s/def ::server-host string?)
(s/def ::http-path string?)

(s/def ::settings (s/keys :req [::nrepl-port
                                ::server-port
                                ::user.spec/username]
                          :opt [::deathstar-dir]))

(s/def ::settings-filepaths (s/coll-of ::filepath
                                       :kind vector? :distinct true :into [] :count nil))

(s/def ::state (s/keys ::req [::settings
                              ::settings-filepaths]))

(def cmd-ids #{"deathstar.open"
               "deathstar.ping"})

(defmacro assert-cmd-id
  [cmd-id]
  (s/assert cmd-ids cmd-id)
  `~cmd-id)