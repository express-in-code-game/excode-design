(ns starnet.pad.datascript1
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

  (def initial-data [[:db/add 1 :name "Petr"]
                     [:db/add 1 :age 44]
                     [:db/add 2 :name "Ivan"]
                     [:db/add 2 :age 25]])
  
  (def test-db
    (let [db (-> (d/empty-db { :age { :db/index true }})
                 (d/db-with initial-data))]
      db))
  
  
  
  


  ;;
  )