(ns starnet.common.alpha.system
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.core :refer [with-gen-fmap]]
   #?(:cljs [starnet.common.alpha.macros :refer-macros [defmethods-for-a-set]]
      :clj  [starnet.common.alpha.macros :refer [defmethods-for-a-set]])))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def :u/uuid uuid?)
(s/def :record/uuid uuid?)
(s/def :u/username string?)
(s/def :u/email (s/with-gen
                  (s/and string? #(re-matches email-regex %))
                  #(sgen/fmap (fn [s]
                                (str s "@gmail.com"))
                              (sgen/such-that (fn [s] (not= s ""))
                                              (sgen/string-alphanumeric)))))

(s/def :u/user (s/keys :req [:u/uuid :u/username :u/email]))

(def setof-ev-event
  #{:ev.c/delete-record :ev.u/create
    :ev.u/update :ev.u/delete
    :ev.g.u/create
    :ev.g.u/delete :ev.g.u/configure
    :ev.g.u/start :ev.g.u/join
    :ev.g.u/leave :ev.g.p/move-cape
    :ev.g.p/collect-tile-value
    :ev.g.a/finish-game})

(s/def :ev/type setof-ev-event)

(s/def :ev.c/delete-record (with-gen-fmap
                             (s/keys :req [:ev/type]
                                     :opt [:record/uuid])
                             #(assoc %  :ev/type :ev.c/delete-record)))

(s/def :ev.u/create (with-gen-fmap
                      (s/keys :req [:ev/type :u/uuid :u/email :u/username]
                              :opt [])
                      #(assoc %  :ev/type :ev.u/create)))

(s/def :ev.u/update (with-gen-fmap
                      (s/keys :req [:ev/type]
                              :opt [:u/email :u/username])
                      #(assoc %  :ev/type :ev.u/update)))

(s/def :ev.u/delete (with-gen-fmap
                      (s/keys :req [:ev/type]
                              :opt [])
                      #(assoc %  :ev/type :ev.u/delete)))

(defmulti ev (fn [x] (:ev/type x)))
(defmethods-for-a-set ev setof-ev-event)
(s/def :ev/event (s/multi-spec ev :ev/type))



(def setof-ev-u-event
  #{:ev.u/create :ev.u/update :ev.u/delete})

(defmulti ev-user (fn [x] (:ev/type x)))
(defmethods-for-a-set ev-user setof-ev-u-event)
(s/def :ev.u/event (s/multi-spec ev-user :ev/type))


(defmulti next-user
  "Returns next state of the user record"
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-user [:ev.u/create]
  [state k ev]
  (or state ev))

(defmethod next-user [:ev.u/update]
  [state k ev]
  (when state
    (merge state ev)))

(defmethod next-user [:ev.u/delete]
  [state k ev]
  nil)

(s/fdef next-user
  :args (s/cat :state (s/nilable :u/user)
               :k uuid?
               :ev :ev.u/event)
  :ret (s/nilable :u/user))

(comment

  (ns-unmap *ns* 'next-user)
  (stest/instrument [`next-user])
  (stest/unstrument [`next-user])

  ;;
  )