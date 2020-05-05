(ns system.iam.spec
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(defn spec-email
  []
  (let []
    (s/with-gen
      (s/and string? #(re-matches email-regex %))
      #(sgen/fmap (fn [s]
                    (str s "@gmail.com"))
                  (gen/such-that (fn [s] (not= s ""))
                                 gen/string-alphanumeric)))))

(defn spec-string-in-range
  [min max & {:keys [gen-char] :or {gen-char gen/char-alphanumeric}}]
  (s/with-gen
    string?
    #(gen/fmap (fn [v] (apply str v)) (gen/vector gen-char min max))))

(comment

  (gen/generate gen/string)
  (gen/generate gen/string-ascii)
  (gen/generate gen/string-alphanumeric)

  (gen/generate (s/gen (spec-string-in-range 4 16 :gen-char gen/char-ascii)))
  (gen/generate (s/gen (spec-string-in-range 4 16 :gen-char gen/char)))

  ;;
  )

(s/def ::url (s/with-gen
               string?
               #(gen/fmap (fn [[a b]] (str a "/" b))
                          (gen/tuple (s/gen #{"github.com" "google.com" "twitch.com"})
                                     (s/gen (spec-string-in-range 4 16 :gen-char gen/char-alphanumeric))))))
(s/def :uuid uuid?)
(s/def :u/username (spec-string-in-range 4 16 :gen-char gen/char-alphanumeric))
(s/def :u/fullname (spec-string-in-range 4 32 :gen-char gen/char-ascii))
(s/def :u/password (spec-string-in-range 8 64 :gen-char gen/char-alphanumeric))
(s/def :u/email (spec-email))
(s/def :u/links  (s/with-gen
                   (s/coll-of ::url)
                   #(gen/vector (s/gen ::url) 0 5)))

(s/def :u/user (s/keys :req [:uuid
                             :u/username
                             :u/email
                             :u/password
                             :u/fullname]
                       :opt [:u/links]))

#_(gen/generate (s/gen :u/user))

