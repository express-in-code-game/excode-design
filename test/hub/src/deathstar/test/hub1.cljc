(ns deathstar.test.hub1
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.pprint :refer [pprint]]
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

(def channels-remote1 (merge
                       (hub.chan/create-channels)
                       {::hub.chan/user| (chan 10)}))

(def channels-remote2 (merge
                       (hub.chan/create-channels)
                       {::hub.chan/user| (chan 10)}))


(def state-remote1 (tap.remote.impl/create-state))
(def state-remote2 (tap.remote.impl/create-state))

(add-watch state-remote1 ::watcher
           (fn [key atom old-state new-state]
             (println ::state-remote1)
             (println (with-out-str (pprint new-state)))))
(add-watch state-remote2 ::watcher
           (fn [key atom old-state new-state]
             (println ::state-remote2)
             (println (with-out-str (pprint new-state)))))

(defn intercept-out|-values
  [response|]
  (fn [value]
    (if (::op.spec/out| value)
      (let [out|* (chan 1)]
        (take! out|*
               (fn [response]
                 (put! response| response)
                 (put! (::op.spec/out| value) response)))
        (assoc value ::op.spec/out| out|*))
      value)))

(def tap-remote1 (let [response| (chan 10)
                       ops| (chan 10 (map (intercept-out|-values response|)))
                       ops|m (mult ops|)
                       ops|t (tap ops|m (chan 10))]
                   (pipe (::hub.chan/ops| channels-remote1) ops|)
                   (pipe (tap ops|m (tap ops|m (chan 10))) (::hub.chan/ops| channels))
                   (pipe response| ops|t)
                   (pipe (::hub.chan/user| channels-remote1) ops|t)
                   (tap.remote.impl/create-proc-ops
                    (merge channels-remote1
                           {::hub.chan/ops| ops|t})
                    state-remote1)))

(def tap-remote2 (let [response| (chan 10)
                       ops| (chan 10 (map (intercept-out|-values response|)))
                       ops|m (mult ops|)
                       ops|t (tap ops|m (chan 10))]
                   (pipe (::hub.chan/ops| channels-remote2) ops|)
                   (pipe (tap ops|m (tap ops|m (chan 10))) (::hub.chan/ops| channels))
                   (pipe response| ops|t)
                   (pipe (::hub.chan/user| channels-remote2) ops|t)
                   (tap.remote.impl/create-proc-ops
                    (merge channels-remote2
                           {::hub.chan/ops| ops|t})
                    state-remote2)))

(comment

  (hub.chan/op
   {::op.spec/op-key ::hub.chan/user-connected}
   channels-remote1
   (::hub.chan/user| channels-remote1))

  (hub.chan/op
   {::op.spec/op-key ::hub.chan/user-join
    ::op.spec/op-type ::op.spec/request}
   channels-remote1
   {::user.spec/uuid (cljc/rand-uuid)})

  (hub.chan/op
   {::op.spec/op-key ::hub.chan/list-users
    ::op.spec/op-type ::op.spec/request}
   channels-remote1)

  ;;
  )

