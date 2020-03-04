(ns ui.alpha.db
  (:require
   [clojure.spec.alpha :as s]
   [re-frame.core :as rf]))

(defn gen-default-db
  "gens the deafult db"
  []
  (let []
    {:ui.alpha.db.core/module-count 0
     :ui.alpha.db.core/active-view nil
     }))

(def default-db (gen-default-db))