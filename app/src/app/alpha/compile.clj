(ns app.alpha.compile
  (:require
   [clojure.repl :as repl]
   [app.kafka.serdes]))

(defn -main
  []
  (println "; compiling")
  #_(alter-var-root #'clojure.core/*compiler-options*
                    #(merge %
                            {:direct-linking false}
                            #_{:disable-locals-clearing true
                               :elide-meta [:doc :file :line :added]}))
  (compile 'app.kafka.serdes))