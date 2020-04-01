(ns starnet.alpha.compile
  (:require
   [clojure.repl :as repl]
   [starnet.alpha.aux.serdes]))

(defn -main
  []
  (println "; compiling")
  #_(alter-var-root #'clojure.core/*compiler-options*
                    #(merge %
                            {:direct-linking false}
                            #_{:disable-locals-clearing true
                               :elide-meta [:doc :file :line :added]}))
  (compile 'starnet.alpha.aux.serdes))