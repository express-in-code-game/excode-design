(ns starnet.app.crux-samples.core
  (:require
   [clojure.repl :refer [doc]]
   [crux.api :as crux]
   [clojure.java.io :as io]))

(def data-dir "/tmp/crux-store")

(comment

  (defn start-standalone-node ^crux.api.ICruxAPI [storage-dir]
    (crux/start-node {:crux.node/topology '[crux.standalone/topology]
                      :crux.kv/db-dir (str (io/file storage-dir "db"))}))

  (def node (start-standalone-node data-dir))

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

  (.close node)
  

  ;;
  )

(comment

  (def crux
    (crux/start-node
     {:crux.node/topology :crux.standalone/topology
      :crux.node/kv-store "crux.kv.memdb/kv"
      :crux.standalone/event-log-dir (str data-dir "/eventlog-1")
      :crux.kv/db-dir (str data-dir "/db-dir")
      :crux.standalone/event-log-kv-store "crux.kv.memdb/kv"}))

  (def manifest
    {:crux.db/id :manifest
     :pilot-name "Johanna"
     :id/rocket "SB002-sol"
     :id/employee "22910x2"
     :badges "SETUP"
     :cargo ["stereo" "gold fish" "slippers" "secret note"]})
  
  (crux/submit-tx crux [[:crux.tx/put manifest]])
  
  (crux/entity (crux/db crux) :manifest)
  
  (.close crux)


  ;;
  )



