(ns DeathStarGame.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.spec.alpha :as s]
   [clojure.java.io :as io]

   [cljfx.api :as fx]
   [datahike.api]
   [taoensso.timbre :as log]
   [taoensso.timbre.appenders.3rd-party.rotor]

   [cljctools.fs.runtime.core :as fs.runtime.core]

   [DeathStarGame.spec]
   [DeathStarGame.cljfx]
   [DeathStarGame.db]))

(println "clojure.compiler.direct-linking" (System/getProperty "clojure.compiler.direct-linking"))
(clojure.spec.alpha/check-asserts true)
(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(log/merge-config! {:level :debug
                    :min-level :info
                    :appenders {:rotating (taoensso.timbre.appenders.3rd-party.rotor/rotor-appender
                                           {:path
                                            (let [peer-index 1]
                                              (->
                                               (io/file (System/getProperty "user.dir") "volumes" (format "peer%s" peer-index) "db-server.log")
                                               (.getCanonicalFile)))
                                            :max-size (* 512 1024)
                                            :backlog 10})}})

(defn stage
  [{:as opts
    :keys [::searchS]}]
  {:fx/type :stage
   :showing true
   #_:on-close-request #_(fn [^WindowEvent event]
                           (println :on-close-request)
                           #_(.consume event))
   :width 1024
   :height 768
   :icons [(str (io/resource "logo/logo.png"))]
   :scene {:fx/type :scene
           :root {:fx/type :h-box
                  :children [{:fx/type :label :text "DeathStarGame"}]}}})

(defonce stateA (atom nil))

(defn -main [& args]
  (println ::-main)
  (let [data-dir (fs.runtime.core/path-join (System/getProperty "user.dir"))
        renderer (cljfx.api/create-renderer)]
    (reset! stateA {:fx/type stage
                    ::renderer renderer})
    (add-watch stateA :watch-fn (fn [k stateA old-state new-state] (renderer new-state)))

    (javafx.application.Platform/setImplicitExit true)
    (renderer @stateA)
    #_(cljfx.api/mount-renderer stateA render)

    (go)))


(comment

  (require
   '[DeathStarGame.db]
   '[datahike.api]
   :reload)

  (do
    (let [peer-index 1]
      (io/make-parents (System/getProperty "user.dir") "volumes" (format "peer%s" peer-index) "db"))
    (def cfg {:store {:backend :file
                      :path "./volumes/peer1/db"}})
    (datahike.api/create-database cfg)
    (def conn (datahike.api/connect cfg)))

  (do
    (datahike.api/release conn)
    (datahike.api/delete-database cfg))

  (datahike.api/transact conn [{:db/ident :name
                                :db/valueType :db.type/string
                                :db/cardinality :db.cardinality/one}
                               {:db/ident :age
                                :db/valueType :db.type/long
                                :db/cardinality :db.cardinality/one}])

  (datahike.api/transact conn [{:name  "Alice", :age   20}
                               {:name  "Bob", :age   30}
                               {:name  "Charlie", :age   40}
                               {:age 15}])

  (datahike.api/q '[:find ?e ?n ?a
                    :where
                    [?e :name ?n]
                    [?e :age ?a]]
                  @conn)

  (datahike.api/transact conn {:tx-data [{:db/id 3 :age 25}]})

  (datahike.api/q {:query '{:find [?e ?n ?a]
                            :where [[?e :name ?n]
                                    [?e :age ?a]]}
                   :args [@conn]})

  (datahike.api/q '[:find ?a
                    :where
                    [?e :name "Alice"]
                    [?e :age ?a]]
                  (datahike.api/history @conn))

  ;
  )