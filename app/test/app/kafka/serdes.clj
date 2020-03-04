(ns app.kafka.serdes
  (:require
   [cognitect.transit :as transit])
  (:import
   java.io.ByteArrayInputStream
   java.io.ByteArrayOutputStream
   org.apache.kafka.common.serialization.Serializer
   org.apache.kafka.common.serialization.Deserializer
   org.apache.kafka.common.serialization.Serde
   org.apache.kafka.common.errors.SerializationException
   java.io.IOException)
  (:gen-class))

#_(compile 'app.kafka.serdes)

(comment

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

(defn transit-write-bytes
  [format data]
  (let [out (ByteArrayOutputStream. #_4096)
        writer (transit/writer out format)]
    (transit/write writer data)
    (.toByteArray out)))

(defn transit-read-bytes
  [format data]
  (let [in (ByteArrayInputStream. data)
        reader (transit/reader in format)]
    (transit/read reader)))

#_(transit-read-bytes :json (transit-write-bytes :json #{1 2 3}))

; https://kafka.apache.org/24/documentation/streams/developer-guide/datatypes.html#implementing-custom-serdes
; https://github.com/apache/kafka/tree/2.4/clients/src/main/java/org/apache/kafka/common/serialization


(deftype TransitJsonSerializer
         []
  Serializer
  (configure [this _ _])
  (close [this])
  (serialize [this topic data]
    (try
      (when data (transit-write-bytes :json data))
      (catch IOException e
        (throw (SerializationException. "Error serializing data with TransitSerializer" e))))))

(deftype TransitJsonDeserializer
         []
  Deserializer
  (configure [this _ _])
  (close [this])
  (deserialize [this topic data]
    (try
      (when data (transit-read-bytes :json data))
      (catch IOException e
        (throw (SerializationException. "Error deserializing data with TransitDeserializer" e))))))

(comment
  (def se (TransitJsonSerializer.))
  (def de (TransitJsonDeserializer.))
  (.deserialize de "asd" (.serialize se "asd" #{1 2 3}))
  ;
  )

(deftype TransitJsonSerde
         []
  Serde
  (configure [this _ _])
  (close [this])
  (serializer [this]
    (TransitJsonSerializer.))
  (deserializer [this]
    (TransitJsonDeserializer.)))



