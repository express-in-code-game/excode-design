(ns system.db.api
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [cljctools.dgraph.client :as dg]
   [clojure.data.json :as json]))


(defn create-client
  [opts]
  (dg/create-client opts))

(defn connect
  [client]
  (dg/connect client))

(defn create-user
  [cl data]
  (let [query (format
               "
                query {
                  var(func: eq(<u/username>,\"%s\" )) {
                    user1 as uid
                  }
                }
                "
               (:u/username data))
        mu {:data (merge {:uid "uid(user1)"
                          "dgraph.type" "User"}
                         (select-keys data [:uuid
                                            :u/username
                                            :u/email
                                            :u/fullname
                                            :u/links
                                            :u/password
                                            :u/password-TMP]))
            ; :condition "@if(eq(len(user1), 0))"
            }]
    (prn mu)
    (dg/upsert cl {:query query
                   :mutations [mu]})))

(defn list-users
  [cl opts]
  (let [query (format
               "
               {
  query(func: has(<u/username>) ) {
    expand(_all_)
    
  }
  
}
                ")]

    (dg/query cl {:query query
                  :vars {}})))

(defn get-user
  [cl opts]
  (let []
    (dg/query cl {})))


(defn list-events
  [cl opts]
  (let []
    (dg/query cl {})))


(comment

  (def cl (create-client {:connections [{:hostname "alpha"
                                         :port 9080}]}))
  (connect cl)


  (def data {:uuid (str (java.util.UUID/randomUUID))
             :u/username "user4"
             :u/email "user1@gmail.com"
             :u/fullname "user one"
             :u/links []
             :u/password "asdasd"
             :u/password-TMP "pass"})

  (alts!! [(create-user cl data) (timeout 5000)])

  (alts!! [(list-users cl {}) (timeout 5000)])

  (alts!! [(dg/q-count-attr cl "u/username") (timeout 1000)])




  ;;
  )