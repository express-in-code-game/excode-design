(ns deathstar.test.hub1
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [is run-all-tests testing deftest run-tests]]

   [cljctools.csp.op.spec :as op.spec]

   [deathstar.hub.chan :as hub.chan]
   [deathstar.hub.impl :as hub.impl]

   [deathstar.user.spec :as user.spec]

   [deathstar.hub.tap.remote.spec :as tap.remote.spec]
   [deathstar.hub.tap.remote.impl :as tap.remote.impl]))


(def channels (merge
               (hub.chan/create-channels)))

(def hub-state (hub.impl/create-state))

(def hub (hub.impl/create-proc-ops channels hub-state {}))


(comment
  
  

  (hub.chan/op
   {::op.spec/op-key ::hub.chan/user-join
    ::op.spec/op-type ::op.spec/request}
   channels
   {::user.spec/uuid (cljc/rand-uuid)})

  (hub.chan/op
   {::op.spec/op-key ::hub.chan/list-users
    ::op.spec/op-type ::op.spec/request}
   channels)

  ;;
  )

