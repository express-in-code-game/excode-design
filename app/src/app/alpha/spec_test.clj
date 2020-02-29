(ns app.alpha.spec-test
  (:require [clojure.pprint :as pp]
            [app.alpha.spec :as s]
            [app.alpha.data.game]
            [app.alpha.data.user]
            [clojure.spec.test.alpha :as stest]))

(def spec-fdefs ['app.alpha.streams.core/create-user
                 'app.alpha.streams.core/produce-event
                 'app.alpha.data.game/next-state
                 'app.alpha.data.user/next-state])

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