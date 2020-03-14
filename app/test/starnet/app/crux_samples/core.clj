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

  ; quotiing

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

  ; name

  (crux/q (crux/db crux)
          '{:find [name]
            :where [[e :type :element/metal]
                    [e :common-name name]]})

  ; more info

  (crux/q (crux/db crux)
          '{:find [name rho]
            :where [[e :density rho]
                    [e :common-name name]]})

  ; arguments

  (crux/q (crux/db crux)
          {:find '[name]
           :where '[[e :type t]
                    [e :common-name name]]
           :args [{'t :element/metal}]})


  (defn filter-type
    [type]
    (crux/q (crux/db crux)
            {:find '[name]
             :where '[[e :type t]
                      [e :common-name name]]
             :args [{'t type}]}))

  (defn filter-appearance
    [description]
    (crux/q (crux/db crux)
            {:find '[name IUPAC]
             :where '[[e :common-name name]
                      [e :IUPAC-name IUPAC]
                      [e :appearance appearance]]
             :args [{'appearance description}]}))

  (filter-type :element/metal)
  (filter-appearance "white solid")

  (crux/submit-tx
   crux
   [[:crux.tx/put
     (assoc manifest :badges ["SETUP" "PUT" "DATALOG-QUERIES"])]])

  (crux/submit-tx
   crux
   [[:crux.tx/put
     {:crux.db/id :consumer/RJ29sUU
      :consumer-id :RJ29sUU
      :first-name "Jay"
      :last-name "Rose"
      :cover? true
      :cover-type :Full}
     #inst "2114-12-03"]])

  (crux/submit-tx
   crux
   [[:crux.tx/put
     {:crux.db/id :consumer/RJ29sUU
      :consumer-id :RJ29sUU
      :first-name "Jay"
      :last-name "Rose"
      :cover? true
      :cover-type :Full}
     #inst "2113-12-03" ;; Valid time start
     #inst "2114-12-03"] ;; Valid time end

    [:crux.tx/put
     {:crux.db/id :consumer/RJ29sUU
      :consumer-id :RJ29sUU
      :first-name "Jay"
      :last-name "Rose"
      :cover? true
      :cover-type :Full}
     #inst "2112-12-03"
     #inst "2113-12-03"]

    [:crux.tx/put
     {:crux.db/id :consumer/RJ29sUU
      :consumer-id :RJ29sUU
      :first-name "Jay"
      :last-name "Rose"
      :cover? false}
     #inst "2112-06-03"
     #inst "2112-12-02"]

    [:crux.tx/put
     {:crux.db/id :consumer/RJ29sUU
      :consumer-id :RJ29sUU
      :first-name "Jay"
      :last-name "Rose"
      :cover? true
      :cover-type :Promotional}
     #inst "2111-06-03"
     #inst "2112-06-03"]])


  (crux/q (crux/db crux #inst "2115-07-03")
          '{:find [cover type]
            :where [[e :consumer-id :RJ29sUU]
                    [e :cover? cover]
                    [e :cover-type type]]})

  (crux/q (crux/db crux #inst "2111-07-03")
          '{:find [cover type]
            :where [[e :consumer-id :RJ29sUU]
                    [e :cover? cover]
                    [e :cover-type type]]})

  (crux/q (crux/db crux #inst "2112-07-03")
          '{:find [cover type]
            :where [[e :consumer-id :RJ29sUU]
                    [e :cover? cover]
                    [e :cover-type type]]})

  (crux/submit-tx
   crux
   [[:crux.tx/put
     (assoc manifest :badges ["SETUP" "PUT" "DATALOG-QUERIES" "BITEMP"])]])


  (easy-ingest
   crux
   [{:crux.db/id :gold-harmony
     :company-name "Gold Harmony"
     :seller? true
     :buyer? false
     :units/Au 10211
     :credits 51}

    {:crux.db/id :tombaugh-resources
     :company-name "Tombaugh Resources Ltd."
     :seller? true
     :buyer? false
     :units/Pu 50
     :units/N 3
     :units/CH4 92
     :credits 51}

    {:crux.db/id :encompass-trade
     :company-name "Encompass Trade"
     :seller? true
     :buyer? true
     :units/Au 10
     :units/Pu 5
     :units/CH4 211
     :credits 1002}

    {:crux.db/id :blue-energy
     :seller? false
     :buyer? true
     :company-name "Blue Energy"
     :credits 1000}])


  (defn stock-check
    [company-id item]
    {:result (crux/q (crux/db crux)
                     {:find '[name funds stock]
                      :where ['[e :company-name name]
                              '[e :credits funds]
                              ['e item 'stock]]
                      :args [{'e company-id}]})
     :item item})


  (defn format-stock-check
    [{:keys [result item] :as stock-check}]
    (for [[name funds commod] result]
      (str "Name: " name ", Funds: " funds ", " item " " commod)))

  (crux/submit-tx
   crux
   [[:crux.tx/cas
   ;; Old doc
     {:crux.db/id :blue-energy
      :seller? false
      :buyer? true
      :company-name "Blue Energy"
      :credits 1000}
   ;; New doc
     {:crux.db/id :blue-energy
      :seller? false
      :buyer? true
      :company-name "Blue Energy"
      :credits 900
      :units/CH4 10}]

    [:crux.tx/cas
   ;; Old doc
     {:crux.db/id :tombaugh-resources
      :company-name "Tombaugh Resources Ltd."
      :seller? true
      :buyer? false
      :units/Pu 50
      :units/N 3
      :units/CH4 92
      :credits 51}
   ;; New doc
     {:crux.db/id :tombaugh-resources
      :company-name "Tombaugh Resources Ltd."
      :seller? true
      :buyer? false
      :units/Pu 50
      :units/N 3
      :units/CH4 82
      :credits 151}]])

  (format-stock-check (stock-check :tombaugh-resources :units/CH4))
  (format-stock-check (stock-check :blue-energy :units/CH4))

  (crux/submit-tx
   crux
   [[:crux.tx/cas
   ;; Old doc
     {:crux.db/id :gold-harmony
      :company-name "Gold Harmony"
      :seller? true
      :buyer? false
      :units/Au 10211
      :credits 51}
   ;; New doc
     {:crux.db/id :gold-harmony
      :company-name "Gold Harmony"
      :seller? true
      :buyer? false
      :units/Au 211
      :credits 51}]

    [:crux.tx/cas
   ;; Old doc
     {:crux.db/id :encompass-trade
      :company-name "Encompass Trade"
      :seller? true
      :buyer? true
      :units/Au 10
      :units/Pu 5
      :units/CH4 211
      :credits 100002}
   ;; New doc
     {:crux.db/id :encompass-trade
      :company-name "Encompass Trade"
      :seller? true
      :buyer? true
      :units/Au 10010
      :units/Pu 5
      :units/CH4 211
      :credits 1002}]])

  (format-stock-check (stock-check :gold-harmony :units/Au))
  (format-stock-check (stock-check :encompass-trade :units/Au))

  (crux/submit-tx
   crux
   [[:crux.tx/put
     (assoc manifest :badges ["SETUP" "PUT" "DATALOG-QUERIES" "BITEMP" "CAS"])]])

  (crux/q (crux/db crux)
          {:find '[belongings]
           :where '[[e :cargo belongings]]
           :args [{'belongings "secret note"}]})


  (crux/submit-tx crux
                  [[:crux.tx/put {:crux.db/id :kaarlang/clients
                                  :clients [:encompass-trade]}
                    #inst "2110-01-01T09"
                    #inst "2111-01-01T09"]

                   [:crux.tx/put {:crux.db/id :kaarlang/clients
                                  :clients [:encompass-trade :blue-energy]}
                    #inst "2111-01-01T09"
                    #inst "2113-01-01T09"]

                   [:crux.tx/put {:crux.db/id :kaarlang/clients
                                  :clients [:blue-energy]}
                    #inst "2113-01-01T09"
                    #inst "2114-01-01T09"]

                   [:crux.tx/put {:crux.db/id :kaarlang/clients
                                  :clients [:blue-energy :gold-harmony :tombaugh-resources]}
                    #inst "2114-01-01T09"
                    #inst "2115-01-01T09"]])

  (crux/history-ascending
   (crux/db crux)
   (crux/new-snapshot (crux/db crux #inst "2116-01-01T09"))
   :kaarlang/clients)

  (crux/submit-tx crux
                  [[:crux.tx/delete :kaarlang/clients #inst "2110-01-01" #inst "2116-01-01"]])

  (crux/history-ascending
   (crux/db crux)
   (crux/new-snapshot (crux/db crux #inst "2116-01-01T09"))
   :kaarlang/clients)



  (crux/submit-tx crux
                  [[:crux.tx/put
                    {:crux.db/id :person/kaarlang
                     :full-name "Kaarlang"
                     :origin-planet "Mars"
                     :identity-tag :KA01299242093
                     :DOB #inst "2040-11-23"}]

                   [:crux.tx/put
                    {:crux.db/id :person/ilex
                     :full-name "Ilex Jefferson"
                     :origin-planet "Venus"
                     :identity-tag :IJ01222212454
                     :DOB #inst "2061-02-17"}]

                   [:crux.tx/put
                    {:crux.db/id :person/thadd
                     :full-name "Thad Christover"
                     :origin-moon "Titan"
                     :identity-tag :IJ01222212454
                     :DOB #inst "2101-01-01"}]

                   [:crux.tx/put
                    {:crux.db/id :person/johanna
                     :full-name "Johanna"
                     :origin-planet "Earth"
                     :identity-tag :JA012992129120
                     :DOB #inst "2090-12-07"}]])


  (defn full-query
    [node]
    (crux/q
     (crux/db node)
     '{:find [id]
       :where [[e :crux.db/id id]]
       :full-results? true}))

  (full-query crux)

  (crux/submit-tx crux [[:crux.tx/evict :person/kaarlang]])

  (crux/history-descending (crux/db crux)
                           (crux/new-snapshot (crux/db crux))
                           :person/kaarlang)

  ;;
  )






