(ns dev.compile
  (:require [clojure.repl :as repl]
            [app.kafka.serdes]))

(defn -main
  []
  (println "; compiling")
  #_(alter-var-root #'clojure.core/*compiler-options*
                    #(merge % {; :disable-locals-clearing true
                          ; :elide-meta [:doc :file :line :added]
                               :direct-linking false}))
  (compile 'app.kafka.serdes))