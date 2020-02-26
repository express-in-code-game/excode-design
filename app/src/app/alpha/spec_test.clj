(ns app.alpha.spec-test
  (:require [clojure.pprint :as pp]
            [app.alpha.spec :as s]
            [clojure.spec.test.alpha :as stest]))

(def spec-fdefs ['app.alpha.streams.users/create-user
                 'app.alpha.streams.users/produce-event])

(comment

  (type `create-user)
  (type 'create-user)

  ;;
  )

(defn instrument
  []
  (stest/instrument spec-fdefs))

(defn unstrument
  []
  (stest/unstrument spec-fdefs))