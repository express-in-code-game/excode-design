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

  ; https://juxt.pro/blog/posts/crux-tutorial-setup.html

  (def crux
    (crux/start-node
     {:crux.node/topology :crux.standalone/topology
      :crux.node/kv-store "crux.kv.memdb/kv"
      :crux.standalone/event-log-dir (str data-dir "/eventlog-1")
      :crux.kv/db-dir (str data-dir "/db-dir")
      :crux.standalone/event-log-kv-store "crux.kv.memdb/kv"}))

  (.close crux)

  (def manifest
    {:crux.db/id :manifest
     :pilot-name "Johanna"
     :id/rocket "SB002-sol"
     :id/employee "22910x2"
     :badges "SETUP"
     :cargo ["stereo" "gold fish" "slippers" "secret note"]})

  (crux/submit-tx crux [[:crux.tx/put manifest]])

  (crux/entity (crux/db crux) :manifest)


  ; https://juxt.pro/blog/posts/crux-tutorial-put.html

  (crux/submit-tx crux
                  [[:crux.tx/put
                    {:crux.db/id :commodity/Pu
                     :common-name "Plutonium"
                     :type :element/metal
                     :density 19.816
                     :radioactive true}]

                   [:crux.tx/put
                    {:crux.db/id :commodity/N
                     :common-name "Nitrogen"
                     :type :element/gas
                     :density 1.2506
                     :radioactive false}]

                   [:crux.tx/put
                    {:crux.db/id :commodity/CH4
                     :common-name "Methane"
                     :type :molecule/gas
                     :density 0.717
                     :radioactive false}]])

  (crux/submit-tx crux
                  [[:crux.tx/put
                    {:crux.db/id :stock/Pu
                     :commod :commodity/Pu
                     :weight-ton 21}
                    #inst "2115-02-13T18"] ;; valid-time

                   [:crux.tx/put
                    {:crux.db/id :stock/Pu
                     :commod :commodity/Pu
                     :weight-ton 23}
                    #inst "2115-02-14T18"]

                   [:crux.tx/put
                    {:crux.db/id :stock/Pu
                     :commod :commodity/Pu
                     :weight-ton 22.2}
                    #inst "2115-02-15T18"]

                   [:crux.tx/put
                    {:crux.db/id :stock/Pu
                     :commod :commodity/Pu
                     :weight-ton 24}
                    #inst "2115-02-18T18"]

                   [:crux.tx/put
                    {:crux.db/id :stock/Pu
                     :commod :commodity/Pu
                     :weight-ton 24.9}
                    #inst "2115-02-19T18"]])

  (crux/submit-tx crux
                  [[:crux.tx/put
                    {:crux.db/id :stock/N
                     :commod :commodity/N
                     :weight-ton 3}
                    #inst "2115-02-13T18" ;; start valid-time
                    #inst "2115-02-19T18"] ;; end valid-time

                   [:crux.tx/put
                    {:crux.db/id :stock/CH4
                     :commod :commodity/CH4
                     :weight-ton 92}
                    #inst "2115-02-15T18"
                    #inst "2115-02-19T18"]])

  (crux/entity (crux/db crux #inst "2115-02-14") :stock/Pu)
  (crux/entity (crux/db crux #inst "2115-02-18") :stock/Pu)

  (defn easy-ingest
    "Uses Crux put transaction to add a vector of documents to a specified
  node"
    [node docs]
    (crux/submit-tx node
                    (vec (for [doc docs]
                           [:crux.tx/put doc]))))

  (crux/submit-tx
   crux
   [[:crux.tx/put
     (assoc manifest :badges ["SETUP" "PUT"])]])

  (crux/entity (crux/db crux) :manifest)

  (easy-ingest
   crux
   [{:crux.db/id :commodity/Pu
     :common-name "Plutonium"
     :type :element/metal
     :density 19.816
     :radioactive true}

    {:crux.db/id :commodity/N
     :common-name "Nitrogen"
     :type :element/gas
     :density 1.2506
     :radioactive false}

    {:crux.db/id :commodity/CH4
     :common-name "Methane"
     :type :molecule/gas
     :density 0.717
     :radioactive false}

    {:crux.db/id :commodity/Au
     :common-name "Gold"
     :type :element/metal
     :density 19.300
     :radioactive false}

    {:crux.db/id :commodity/C
     :common-name "Carbon"
     :type :element/non-metal
     :density 2.267
     :radioactive false}

    {:crux.db/id :commodity/borax
     :common-name "Borax"
     :IUPAC-name "Sodium tetraborate decahydrate"
     :other-names ["Borax decahydrate" "sodium borate" "sodium tetraborate" "disodium tetraborate"]
     :type :mineral/solid
     :appearance "white solid"
     :density 1.73
     :radioactive false}])


  (crux/q (crux/db crux)
          '{:find [element]
            :where [[element :type :element/metal]]})
  
  (=
   (crux/q (crux/db crux)
           '{:find [element]
             :where [[element :type :element/metal]]})

   (crux/q (crux/db crux)
           {:find '[element]
            :where '[[element :type :element/metal]]})

   (crux/q (crux/db crux)
           (quote
            {:find [element]
             :where [[element :type :element/metal]]})))
  
  (crux/q (crux/db crux)
          '{:find [name]
            :where [[e :type :element/metal]
                    [e :common-name name]]})
  
  (crux/q (crux/db crux)
          '{:find [name rho]
            :where [[e :density rho]
                    [e :common-name name]]})
  
  


  ;;
  )






