(ns starnet.app.crux-samples.core
  (:require
   [clojure.repl :refer [doc]]
   [crux.api :as crux]
   [clojure.java.io :as io]))

(comment

  (defn start-standalone-node ^crux.api.ICruxAPI [storage-dir]
    (crux/start-node {:crux.node/topology '[crux.standalone/topology]
                      :crux.kv/db-dir (str (io/file storage-dir "db"))}))

  (def node (start-standalone-node "/tmp/crux-store"))

  (crux/submit-tx
   node
   [[:crux.tx/put
     {:crux.db/id :dbpedia.resource/Pablo-Picasso ; id
      :name "Pablo"
      :last-name "Picasso"}
     #inst "2018-05-18T09:20:27.966-00:00"]]) ; valid time

  (crux/q (crux/db node)
          '{:find [e]
            :where [[e :name "Pablo"]]})

  #_#{[:dbpedia.resource/Pablo-Picasso]}

  (crux/entity (crux/db node) :dbpedia.resource/Pablo-Picasso)

  #_{:crux.db/id :dbpedia.resource/Pablo-Picasso
     :name "Pablo"
     :last-name "Picasso"}


  ;;
  )



