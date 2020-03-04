(ns app.alpha.main
  (:require
   [dev.nrepl :refer [start-nrepl-server]]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [common.alpha.spec]
   [common.alpha.tests]
   [common.sample-tests]
   
   [app.alpha.spec]
   [app.alpha.repl]
   [app.alpha.tests]
   [app.sample-tests]))

(defn env-optimized?
  []
  (let [appenv (read-string (System/getenv "appenv"))]
    (:optimized appenv)))

(defn -main  [& args]
  (start-nrepl-server "0.0.0.0" 7788)
  (when-not (env-optimized?)
    (stest/instrument)
    (s/check-asserts true))
  (when (env-optimized?)
    (alter-var-root #'clojure.test/*load-tests* (fn [_] false)))
  #_(app.alpha.repl/mount))

(comment
  
  (stest/unstrument)
  
  ;;
  )