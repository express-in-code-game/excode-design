(ns user
  (:require
   [clojure.repl :refer [dir doc]]
   [shadow.cljs.devtools.api :as shadow]))

(defn abc [])

(comment

  ; api
  (dir shadow)

  (shadow/compile :app)


  ;;
  )