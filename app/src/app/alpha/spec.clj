(ns app.alpha.spec
  (:require [clojure.pprint :as pp]
            [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest])
  (:import java.util.Date))


(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(s/def :user/uuid uuid?)
(s/def :user/username string?)
(s/def :user/email (s/and string? #(re-matches email-regex %)))

(s/def :user/user (s/keys :req [:user/uuid :user/username :user/email]))

(s/def :event/create-user (s/and (s/keys :req [:event/type :user/uuid :user/email :user/username]
                                         :opt [])
                                 #(= (:event/type %) :event/create-user)))

(s/def :event/update-user (s/and (s/keys :req []
                                         :opt [:user/email :user/username])
                                 #(= (:event/type %) :event/update-user)
                                 #(not-empty (select-keys % [:user/email :user/username]))))

(comment
  (s/valid? :event/create-user
            {:event/type :event/create-user
             :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
             :user/email "user0@gmail.com"
             :user/username "user0"})
  
  (s/explain :event/update-user
             {:event/type :event/update-user
              :user/email "user0@gmail.com"
              :user/username "user0"})

  ;;
  )

(comment

  (defmulti event-type (fn [x] (:event/type x)))
  (defmethod event-type :event/create-user [x]
    (s/keys :req [:event/type :user/uuid :user/email :user/username]))
  (defmethod event-type :event/update-user [x]
    (s/keys :opt [:event/type :user/email :user/username]))

  (s/def :event/event (s/multi-spec event-type :event/type))

  (s/valid? :event/event
            {:event/type :event/create-user
             :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
             :user/email "user0@gmail.com"
             :user/username "user0"})

  (s/conform :event/event
             {:event/type :event/create-user
              :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :user/email "user0@gmail.com"
              :user/username "user0"})

  (s/explain :event/event
             {:event/type :event/create-user
              :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :user/username "user0"})

  (s/conform :event/event
             {:event/type :event/create-user
              :user/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :user/username "user0"})

  (s/valid? :event/event
            {:event/type :event/update-user
             :user/email "user0@gmail.com"
             :user/username "user0"})
  ;;
  )

(comment

  (uuid?  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
  (type #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
  (java.util.UUID/fromString "5ada3765-0393-4d48-bad9-fac992d00e62")




  ;;
  )
