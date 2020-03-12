(ns starnet.common.pad.datascript1
  (:require
   [datascript.core :as ds]))

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