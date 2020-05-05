(ns system.dgraph.app.main
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [cljctools.dgraph.client :as dg]
   [system.dgraph.api]))


(defn -main [& args]
  (println "abc"))

(comment


  (def cl (dg/create-client {:connections [{:hostname          "alpha"
                                            :port              9080}]}))
  (dg/connect cl)
  (dg/release cl)


  (def schema (slurp "../src/system/dgraph/app/src/system/dgraph/app/schema"))

  (alts!! [(dg/alter cl {:schema schema}) (timeout 10000)])

  (alts!! [(dg/q-schema cl) (timeout 1000)])

  (alts!! [(dg/drop-all cl) (timeout 10000)])

  (alts!! [(dg/q-count-attr cl "u/username") (timeout 1000)])






  ;;
  )

