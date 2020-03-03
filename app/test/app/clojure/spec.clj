(ns app.clojure.spec
  (:require [clojure.pprint :as pp]
            [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest])
  (:import java.util.Date))

(comment
  ; https://github.com/clojure/spec.alpha
  ; https://clojure.org/about/spec
  ; https://clojure.org/guides/spec
  ; https://clojure.github.io/test.check/intro.html
  ; https://clojure.github.io/test.check/generator-examples.html

  (s/conform even? 1000)
  (s/valid? even? 1000)

  (s/valid? nil? nil)
  (s/valid? string? "abc")
  (s/valid? #(> % 5) 10)
  (s/valid? inst? (Date.))
  (s/valid? #{42} 42)

  (s/def ::inst inst?)
  (s/valid? ::inst (Date.))
  (doc ::inst)
  (s/def ::big-even (s/and int? #(> % 1000)))
  (doc ::big-even)
  (s/valid? ::big-even 10000)
  (s/def ::string-or-int (s/or :string string?
                               :int int?))
  (s/valid? ::string-or-int "abc")
  (s/valid? ::string-or-int 1000)
  (s/valid? ::string-or-int :foo)
  (s/conform ::string-or-int "abc")
  (s/conform ::string-or-int 1000)

  (s/valid? string? nil)
  (s/valid? (s/nilable string?) nil)

  (s/explain ::big-even 10)
  (s/explain ::string-or-int :foo)
  (s/explain-str ::string-or-int :foo)
  (s/explain-data ::string-or-int :foo)

  (def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
  (s/def ::email-type (s/and string? #(re-matches email-regex %)))

  (s/def ::acctid int?)
  (s/def ::username string?)
  (s/def ::email ::email-type)
  (s/def ::phone string?)

  (s/def ::user (s/keys :req [::username ::email]
                        :opt [::phone]))
  (s/valid? ::user
            {::username "user1"
             ::email "user1@gmail.com"})
  (s/explain ::user
             {::username "user1"})
  (s/explain-str ::user
                 {::username "user1"
                  ::email "n/a"})

  (s/def :unq/user
    (s/keys :req-un [::username ::email]
            :opt-un [::phone]))
  (s/valid? :unq/user
            {:username "user1"
             :email "user1@gmail.com"})
  (s/conform  :unq/user
              {:username "user1"
               :email "user1@gmail.com"})
  (s/explain :unq/user
             {:username "user1"})
  (s/explain :unq/user
             {:username "user1"
              :email "n/a"})

  (s/def ::port number?)
  (s/def ::host string?)
  (s/def ::id keyword?)
  (s/def ::server (s/keys* :req [::id ::host] :opt [::port]))
  (s/conform ::server [::id :s1 ::host "localhost" ::port 8080])

  (s/def :animal/kind string?)
  (s/def :animal/says string?)
  (s/def :animal/common (s/keys :req [:animal/kind :animal/says]))
  (s/def :dog/tail? boolean?)
  (s/def :dog/breed string?)
  (s/def :animal/dog (s/merge :animal/common
                              (s/keys :req [:dog/tail? :dog/breed])))
  (s/valid? :animal/dog
            {:animal/kind "dog"
             :animal/says "woof"
             :dog/tail? true
             :dog/breed "retriever"})

  ; https://clojure.org/guides/spec#_multi_spec

  (s/def :event/timestamp int?)
  (s/def :search/url string?)
  (s/def :error/message string?)
  (s/def :error/code int?)

  (defmulti event-type :event/type)
  (defmethod event-type :event/search [_]
    (s/keys :req [:event/type :event/timestamp :search/url]))
  (defmethod event-type :event/error [_]
    (s/keys :req [:event/type :event/timestamp :error/message :error/code]))

  (s/def :event/event (s/multi-spec event-type :event/type))

  (s/valid? :event/event
            {:event/type :event/search
             :event/timestamp 1463970123000
             :search/url "https://clojure.org"})

  (s/valid? :event/event
            {:event/type :event/error
             :event/timestamp 1463970123000
             :error/message "Invalid host"
             :error/code 500})

  (s/explain :event/event
             {:event/type :event/restart})

  (s/explain :event/event
             {:event/type :event/search
              :search/url 200})

  ; collection

  (s/conform (s/coll-of keyword?) [:a :b :c])
  (s/conform (s/coll-of number?) #{5 10 2})

  (s/def ::vnum3 (s/coll-of number? :kind vector? :count 3 :distinct true :into #{}))
  (s/conform ::vnum3 [1 2 3])
  (s/explain ::vnum3 #{1 2 3})
  (s/explain ::vnum3 [1 1 1])
  (s/explain ::vnum3 [1 2 :a])

  ; tuple

  (s/def ::point (s/tuple double? double? double?))
  (s/conform ::point [1.5 2.5 -0.5])
  (doc s/cat)

  ; map

  (s/def ::scores (s/map-of string? int?))
  (s/conform ::scores {"Sally" 1000, "Joe" 500})

  ;  sequences

  (s/def ::ingredient (s/cat :quantity number? :unit keyword?))
  (s/conform ::ingredient [2 :teaspoon])
  (s/explain ::ingredient [11 "peaches"])
  (s/explain ::ingredient [2])

  (s/def ::seq-of-keywords (s/* keyword?))
  (s/conform ::seq-of-keywords [:a :b :c])
  (s/explain ::seq-of-keywords [10 20])

  (s/def ::odds-then-maybe-even (s/cat :odds (s/+ odd?)
                                       :even (s/? even?)))
  (s/conform ::odds-then-maybe-even [1 3 5 100])
  (s/conform ::odds-then-maybe-even [1])
  (s/explain ::odds-then-maybe-even [100])

  (s/def ::opts (s/* (s/cat :opt keyword? :val boolean?)))
  (s/conform ::opts [:silent? false :verbose true])

  (s/def ::config (s/*
                   (s/cat :prop string?
                          :val  (s/alt :s string? :b boolean?))))
  (s/conform ::config ["-server" "foo" "-verbose" true "-user" "joe"])

  (s/describe ::seq-of-keywords)
  (s/describe ::odds-then-maybe-even)
  (s/describe ::opts)
  (s/describe ::config)

  (s/def ::even-strings (s/& (s/* string?) #(even? (count %))))
  (s/valid? ::even-strings ["a"])  ;; false
  (s/valid? ::even-strings ["a" "b"])  ;; true
  (s/valid? ::even-strings ["a" "b" "c"])  ;; false
  (s/valid? ::even-strings ["a" "b" "c" "d"])  ;; true

  (s/def ::nested
    (s/cat :names-kw #{:names}
           :names (s/spec (s/* string?))
           :nums-kw #{:nums}
           :nums (s/spec (s/* number?))))
  (s/conform ::nested [:names ["a" "b"] :nums [1 2 3]])

  (s/def ::unnested
    (s/cat :names-kw #{:names}
           :names (s/* string?)
           :nums-kw #{:nums}
           :nums (s/* number?)))
  (s/conform ::unnested [:names "a" "b" :nums 1 2 3])

  ; using spec for validation

  (defn user
    [user]
    {:pre [(s/valid? ::user user)]
     :post [(s/valid? string? %)]}
    (str (::username user) " " (::email user)))
  (user 42)
  (user {::username "user1" ::email "user1_gmail.com"})
  (user {::username "user1" ::email "user1@gmail.com"})

  (defn user2
    [user]
    (let [u (s/assert ::user user)]
      (str (::username u) " " (::email u))))
  (s/check-asserts true)
  (user2 42)
  (user2 {::username "user1" ::email "user1_gmail.com"})
  (user2 {::username "user1" ::email "user1@gmail.com"})
  (doc s/assert)


  (defn- set-config [prop val]
  ;; dummy fn
    (println "set" prop val))

  (defn configure [input]
    (let [parsed (s/conform ::config input)]
      (if (= parsed ::s/invalid)
        (throw (ex-info "Invalid input" (s/explain-data ::config input)))
        (for [{prop :prop [_ val] :val} parsed]
          (set-config (subs prop 1) val)))))

  (configure ["-server" "foo" "-verbose" true "-user" "joe"])


  (defn ranged-rand
    "Returns random int in range start <= rand < end"
    [start end]
    (+ start (long (rand (- end start)))))

  (s/fdef ranged-rand
    :args (s/and (s/cat :start int? :end int?)
                 #(< (:start %) (:end %)))
    :ret int?
    :fn (s/and #(>= (:ret %) (-> % :args :start))
               #(< (:ret %) (-> % :args :end))))
  (doc ranged-rand)
  (ranged-rand 1 3)
  (ranged-rand 5 1)
  (ranged-rand 1 5)
  (stest/instrument `ranged-rand)
  (ranged-rand 5 1)
  (stest/unstrument `ranged-rand)
  
  (stest/check `ranged-rand)

  (defn adder [x] #(+ x %))
  (s/fdef adder
    :args (s/cat :x number?)
    :ret (s/fspec :args (s/cat :y number?)
                  :ret number?)
    :fn #(= (-> % :args :x) ((:ret %) 0)))


  (s/fdef clojure.core/declare
    :args (s/cat :names (s/* simple-symbol?))
    :ret any?)
  (declare 100)

  (defn hello
    [name]
    (str "hello " name))
  (s/fdef hello
    :args string?
    :ret string?)
  (hello "asd")
  (hello :asd)
  (stest/instrument `hello)
  (hello :asd)
  (stest/unstrument `hello)

  (stest/abbrev-result (first (stest/check `ranged-rand)))
  (-> (stest/enumerate-namespace 'app.kafka.spec-example) stest/check)
  (stest/check)


  (defn invoke-service [service request]
  ;; invokes remote service
    )

  (defn run-query [service query]
    (let [{::keys [result error]} (invoke-service service {::query query})]
      (or result error)))

  (s/def ::query string?)
  (s/def ::request (s/keys :req [::query]))
  (s/def ::result (s/coll-of string? :gen-max 3))
  (s/def ::error int?)
  (s/def ::response (s/or :ok (s/keys :req [::result])
                          :err (s/keys :req [::error])))

  (s/fdef invoke-service
    :args (s/cat :service any? :request ::request)
    :ret ::response)

  (s/fdef run-query
    :args (s/cat :service any? :query string?)
    :ret (s/or :ok ::result :err ::error))
  
  (stest/instrument `invoke-service {:stub #{`invoke-service}})
  (invoke-service nil {::query "test"})
  (invoke-service nil {::query "test"})
  (stest/summarize-results (stest/check `run-query))
  

  ; generators

  (gen/generate (s/gen int?))
  (gen/generate (s/gen nil?))
  (gen/sample (s/gen string?))
  (gen/sample (s/gen #{:club :diamond :heart :spade}))
  (gen/sample (s/gen (s/cat :k keyword? :ns (s/+ number?))))

  (s/exercise (s/cat :k keyword? :ns (s/+ number?)) 5)
  (s/exercise (s/or :k keyword? :s string? :n number?) 5)

  (s/exercise-fn `ranged-rand)


  (gen/generate (s/gen even?))
  (gen/generate (s/gen (s/and int? even?)))

  (defn divisible-by [n] #(zero? (mod % n)))
  (gen/sample (s/gen (s/and int?
                            #(> % 0)
                            (divisible-by 3))))

  (gen/sample (s/gen (s/and string? #(clojure.string/includes? % "hello"))))


  ; custom generators

  (s/def ::kws (s/and keyword? #(= (namespace %) "my.domain")))
  (s/valid? ::kws :my.domain/name) ;; true
  (gen/sample (s/gen ::kws)) ;; unlikely we'll generate useful keywords this way

  (def kw-gen (s/gen #{:my.domain/name :my.domain/occupation :my.domain/id}))
  (gen/sample kw-gen 5)

  (s/def ::kws (s/with-gen (s/and keyword? #(= (namespace %) "my.domain"))
                 #(s/gen #{:my.domain/name :my.domain/occupation :my.domain/id})))
  (s/valid? ::kws :my.domain/name)  ;; true
  (gen/sample (s/gen ::kws))
  ;;=> (:my.domain/occupation :my.domain/occupation :my.domain/name  ...)

  (def kw-gen-2 (gen/fmap #(keyword "my.domain" %) (gen/string-alphanumeric)))
  (gen/sample kw-gen-2 5)

  (def kw-gen-3 (gen/fmap #(keyword "my.domain" %)
                          (gen/such-that #(not= % "")
                                         (gen/string-alphanumeric))))
  (gen/sample kw-gen-3 5)


  (s/def ::hello
    (s/with-gen #(clojure.string/includes? % "hello")
      #(gen/fmap (fn [[s1 s2]] (str s1 "hello" s2))
                 (gen/tuple (gen/string-alphanumeric) (gen/string-alphanumeric)))))
  (gen/sample (s/gen ::hello))

  (s/def ::roll (s/int-in 0 11))
  (gen/sample (s/gen ::roll))

  (s/def ::the-aughts (s/inst-in #inst "2000" #inst "2010"))
  (drop 50 (gen/sample (s/gen ::the-aughts) 55))

  (s/def ::dubs (s/double-in :min -100.0 :max 100.0 :NaN? false :infinite? false))
  (s/valid? ::dubs 2.9)
  (s/valid? ::dubs Double/POSITIVE_INFINITY)
  (gen/sample (s/gen ::dubs))



  ;; 
  )

(comment

  (ns-unmap *ns* 'ev-type)
  
  (defmulti ev-type (fn [kw] kw))
  (defmethod ev-type :ev.u/create [_] #{:ev.u/create})
  (defmethod ev-type :ev.u/delete [_] #{:ev.u/delete})
  (s/def :ev/type1 (s/multi-spec ev-type (fn [gened-val dispatch-tag]
                                           (prn "gened-val " gened-val)
                                           (prn "dispatch-tag " dispatch-tag)
                                           dispatch-tag)))
  (ev-type :ev.u/create)
  (s/valid? :ev/type1 :ev.u/create)
  (s/valid? :ev/type1 :ev.u/delete)
  (s/valid? :ev/type1 :ev.u/delete1)
  (gen/generate (s/gen :ev/type1))
  
  ;;
  )