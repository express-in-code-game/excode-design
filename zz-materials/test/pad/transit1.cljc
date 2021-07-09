(ns starnet.pad.transit1
  (:require
   [cognitect.transit :as transit])
  #?(:clj (:import
           java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream
           com.cognitect.transit.WriteHandler
           com.cognitect.transit.ReadHandler)))

#?(:clj (comment

; https://github.com/cognitect/transit-clj/blob/d99c1f31eae8972a705d491dfcce604c533221cf/README.md

          (def out (ByteArrayOutputStream. 4096))
          (def writer (transit/writer out :json))
          (transit/write writer "foo")
          (transit/write writer {:a [1 2]})

          (.toString out)

          (def in (ByteArrayInputStream. (.toByteArray out)))
          (def reader (transit/reader in :json))
          (prn (transit/read reader))
          (prn (transit/read reader))

  ;
          )
   )

#?(:cljs (comment
           ;https://github.com/cognitect/transit-cljs
           ;http://cognitect.github.io/transit-cljs/cognitect.transit.html#var-writer

           (def writer (transit/writer :json))
           (def reader (transit/reader :json))
           (transit/read reader
                         (transit/write writer {:a [1 2]
                                                :b "foo"}))

           (transit/write (transit/writer :json) {:ws/data {:some "data"}})

           (let [transit-str "[\"^ \",\"~:ws/data\",[\"^ \",\"~:some\",\"data\"]]"
                 blob (js/Blob. [transit-str] #js {:type "application/transit+json"})]
             (-> blob
                 (.text)
                 (.then (fn [s]
                          (let [r (transit/reader :json)
                                o (transit/read r s)]
                            (println o))))))

           (let [w (transit/writer :json)
                 s (transit/write w {:ws/data {:some "data"}})
                 blob (js/Blob. [s] #js {:type "application/transit+json"})]
             (-> blob
                 (.arrayBuffer)
                 (.then (fn [ab]
                          (js/console.log ab)))))

  ;
           ))


