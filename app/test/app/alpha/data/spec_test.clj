(ns app.alpha.data.spec-test
  (:require [clojure.pprint :as pp]
            [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]))

; no value.  
; -> tests proptests 

(comment
  ; https://stackoverflow.com/questions/36639154/convert-java-util-date-to-what-java-time-type
  (def d (java.util.Date.))
  (.getTime d)
  (inst? (java.time.Instant/now))
  (inst? (java.time.Instant/now))
  (java.sql.Timestamp. 0)
  (java.sql.Timestamp.)

  (pr-str (java.util.Date.))
  (pr-str (java.time.Instant/now))
  (read-string (pr-str (java.time.Instant/now)))

  (s/explain :g/game {:g/uuid (java.util.UUID/randomUUID)
                      :g/status :created
                      :g/start-inst (java.util.Date.)
                      :g/duration-ms 60000
                      :g/map-size [128 128]
                      :g/roles {#uuid "5ada3765-0393-4d48-bad9-fac992d00e62" {:g.r/host true
                                                                              :g.r/player 0
                                                                              :g.r/observer false}}
                      :g/player-states {0 {:g.p/entities {:g.p/cape {:g.e/type :g.e.type/cape
                                                                     :g.e/uuid (java.util.UUID/randomUUID)
                                                                     :g.e/pos [0 0]}}
                                           :g.p/sum 0}
                                        1 {:g.p/entities {:g.p/cape {:g.e/type :g.e.type/cape
                                                                     :g.e/uuid (java.util.UUID/randomUUID)
                                                                     :g.e/pos [0 15]}}
                                           :g.p/sum 0}}
                      :g/exit-teleports [{:g.e/type :g.e.type/teleport
                                          :g.e/uuid (java.util.UUID/randomUUID)
                                          :g.e/pos [15 0]}
                                         {:g.e/type :g.e.type/teleport
                                          :g.e/uuid (java.util.UUID/randomUUID)
                                          :g.e/pos [15 15]}]
                      :g/value-tiles (-> (mapcat (fn [x]
                                                   (mapv (fn [y]
                                                           {:g.e/uuid (java.util.UUID/randomUUID)
                                                            :g.e/type :g.e.type/value-tile
                                                            :g.e/pos [x y]
                                                            :g.e/numeric-value (inc (rand-int 10))}) (range 0 16)))
                                                 (range 0 16))
                                         (vec))})

  (gen/generate (s/gen :g/game))


  (gen/sample ev-p-move-cape-gen 1)

  (gen/sample (s/gen :u/email))

  (s/def :test/uuids (s/coll-of uuid?))
  (s/explain :test/uuids [(java.util.UUID/randomUUID) (java.util.UUID/randomUUID)])

  ;;
  )



(comment

  (s/def :test/a-keyword keyword?)
  (s/def :test/a-string string?)
  (s/def :test/or (s/or :a :test/a-string :b :test/a-keyword))
  (s/explain :test/or ":as")

  (s/valid? :ev.u/create
            {:ev/type :ev.u/create
             :u/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
             :u/email "user0@gmail.com"
             :u/username "user0"})

  (s/explain :ev.u/update
             {:ev/type :ev.u/update
              :u/email "user0@gmail.com"
              :u/username "user0"})

  (s/conform :ev/event
             {:ev/type :ev.u/create
              :u/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :u/email "user0@gmail.com"
              :u/username "user0"})

  (s/explain :ev.g.p/move-cape
             {:ev/type :ev.g.p/move-cape
              :p/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :g/uuid (java.util.UUID/randomUUID)
              :g.p/cape {:g.e/uuid (java.util.UUID/randomUUID)
                         :g.e/type :g.e.type/cape
                         :g.e/pos [1 1]}})

  (s/explain :ev.g.p/event
             {:ev/type :ev.g.p/move-cape
              :p/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"
              :g/uuid (java.util.UUID/randomUUID)
              :g.p/cape {:g.e/uuid (java.util.UUID/randomUUID)
                         :g.e/type :g.e.type/cape
                         :g.e/pos [1 1]}})

  (s/explain :ev.g/event (first (gen/sample gen-ev-p-move-cape 1)))
  (s/explain :ev.g/event (first (gen/sample gen-ev-a-finish-game 1)))

  (s/explain :ev.c/delete-record
             {:ev/type :ev.c/delete-record
              :record/uuid  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62"})
  (s/explain :ev.c/delete-record
             {:ev/type :ev.c/delete-record})


  (gen/generate (s/gen :ev.g.p/move-cape))

  (gen/generate (s/gen :ev.u/update))

  (s/def ::hello
    (s/with-gen #(clojure.string/includes? % "hello")
      #(gen/fmap (fn [[s1 s2]] (str s1 "hello" s2))
                 (gen/tuple (gen/string-alphanumeric) (gen/string-alphanumeric)))))
  (gen/sample (s/gen ::hello))

  ;;
  )

(comment

  (uuid?  #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
  (type #uuid "5ada3765-0393-4d48-bad9-fac992d00e62")
  (java.util.UUID/fromString "5ada3765-0393-4d48-bad9-fac992d00e62")

  ;;
  )










