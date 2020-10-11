(ns deathstar.extension.spec
  #?(:cljs (:require-macros [deathstar.extension.spec]))
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]

   [deathstar.user.spec :as user.spec]
   [deathstar.server.spec :as server.spec]
   [deathstar.hub.tap.remote.spec :as tap.remote.spec]))

(do (clojure.spec.alpha/check-asserts true))

(s/def ::filepath string?)

(s/def ::connect-to-server keyword?)
(s/def ::servers (s/map-of keyword? ::server.spec/server))
(s/def ::config (s/keys :req [::servers
                              ::connect-to-server
                              ::user.spec/username]
                        :opt []))


(s/def ::state (s/and
                ::config
                (s/keys ::req [])))

(def cmd-ids #{"deathstar.open"
               "deathstar.ping"})

(defmacro assert-cmd-id
  [cmd-id]
  (s/assert cmd-ids cmd-id)
  `~cmd-id)