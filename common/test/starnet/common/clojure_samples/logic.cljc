(ns starnet.common.clojure-samples.logic
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.logic :as l :refer [run* ==]]))

(comment

  (run* [q]
        (== q true))

 ;;
  )

