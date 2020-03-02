(ns app.main
  (:require [dev.nrepl :refer [start-nrepl-server]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [app.kafka.core]
            [app.kafka.wordcount-example]
            [app.kafka.streams-example]
            [app.kafka.transit-example]
            [app.kafka.ktable-agg-example]
            [app.kafka.reduce-example]
            [app.kafka.aggregate-delete-example]
            [app.kafka.serde-compare]
            [app.clojure.spec]
            [app.clojure.multi]
            [app.alpha.repl :as alpha-repl]
   ;
            ))

(defn env-optimized?
  []
  (let [appenv (read-string (System/getenv "appenv"))]
    (:optimized appenv)))

(defn -main  [& args]
  (start-nrepl-server "0.0.0.0" 7788)
  (when-not (env-optimized?)
    (stest/instrument)
    (s/check-asserts true))
  #_(alpha-repl/mount))

(comment
  
  (stest/unstrument)
  
  ;;
  )