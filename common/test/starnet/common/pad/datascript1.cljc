(ns starnet.common.pad.datascript1
  (:require
   [datascript.core :as d]
   [datascript.db :as db]))

(comment

  (let [schema {:aka {:db/cardinality :db.cardinality/many}}
        conn   (ds/create-conn schema)]
    (ds/transact! conn [{:db/id -1
                         :name  "Maksim"
                         :age   45
                         :aka   ["Max Otto von Stierlitz", "Jack Ryan"]}])
    (ds/q '[:find  ?n ?a
            :where [?e :aka "Max Otto von Stierlitz"]
            [?e :name ?n]
            [?e :age  ?a]]
          @conn))



  ;;
  )


(comment
  
  (def schema {:aka {:db/cardinality :db.cardinality/many}})
  
  (def datoms #{(d/datom 1 :age  17)
                (d/datom 1 :name "Lea")})
  
  
  ;;
  )


(comment
  
  
  
  
  ;;
  )