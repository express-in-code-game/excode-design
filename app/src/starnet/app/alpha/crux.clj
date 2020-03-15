(ns starnet.app.alpha.crux
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub]]
   [crux.api :as crux]
   [clojure.java.io :as io]))



(defn easy-ingest
  "Uses Crux put transaction to add a vector of documents to a specified
  node"
  [node docs]
  (crux/submit-tx node
                  (vec (for [doc docs]
                         [:crux.tx/put doc]))))

; not used in the system, for repl purposes only
; this value is set from main/proc-cruxdb
(def ^:private node nil)


(comment

  (easy-ingest
   node
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

  (crux/q (crux/db node)
          '{:find [element]
            :where [[element :type :element/metal]]})

  (.close node)

  ;;
  )