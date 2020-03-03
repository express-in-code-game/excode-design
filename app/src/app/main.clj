(ns app.main
  (:require [dev.nrepl :refer [start-nrepl-server]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [app.alpha.repl]
            [app.alpha.tests]
            [app.pad]))

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
    (alter-var-root #'clojure.test/*load-tests* (fn [v] false)))
  #_(app.alpha.repl/mount))

(comment
  
  (stest/unstrument)
  
  ;;
  )