(ns starnet.common.alpha.game01
  (:refer-clojure :exclude [==])
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]

   [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
   [clojure.core.logic :exclude [is] :refer :all]
   [clojure.core.logic.pldb :as pldb :refer [db with-db db-rel db-fact]]
   [clojure.core.logic.fd  :as fd]
   [clojure.core.logic.unifier :as u]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]))


(def tags #{:entity :cape :knowledge :bio :building :combinable :element})


(s/def :g.e.prop/resolve (s/with-gen int?
                           #(gen/choose 100 1000)))
(s/def :g.e.prop/vision (s/with-gen int?
                          #(gen/choose 4 16)))
(s/def :g.e.prop/energy (s/with-gen int?
                          #(gen/choose 0 100)))
(s/def :g.e/tags (s/with-gen (s/coll-of keyword?)
                   #(gen/list-distinct (gen/elements tags) {:num-elements 3})))

(s/def :g.e/cape (s/keys :req [:g.e.prop/resolve
                               :g.e.prop/vision
                               :g.e.prop/energy
                               :g.e/tags]))

(comment

  (gen/generate (gen/list-distinct (gen/elements tags) {:num-elements 3}))
  (gen/generate (s/gen :g.e/tags))
  (gen/generate (s/gen :g.e/cape))

  (= (fd/-intersection (fd/interval 0 20) (fd/interval 10 30)) (fd/interval 10 20))

  ;;
  )



