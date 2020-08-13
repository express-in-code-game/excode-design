(ns mult.impl.conf
  (:require
   [goog.string :refer [format]]
   [clojure.string :as string]
   [clojure.pprint :refer [pprint]]
   [cljs.reader :refer [read-string]]
   [clojure.walk :as walk]))



(defn preprocess
  [conf]
  (let []
    (-> conf
        (update :connections #(->> (map (fn [[k v]]
                                          [k (merge v {:id k})])  %)
                                   (into {}))))))

(defn evaluate
  [conf]
  (let []
    (walk/postwalk (fn [x]
                     (if (and (list? x) (= (first x) 'fn))
                       (eval x)
                       x))  conf)))

(defn filepath->lrepl-ids
  "Returns lrepls that include that file"
  [conf* filepath]
  (into []
        (comp
         (filter (fn [[id data]]
                   ((:pred/include-file? data) filepath)))
         (map (fn [[id data]] id)))
        (:repls conf*)))

(defn filepath->tab-ids
  "Returns lrepls that include that file"
  [conf* filepath]
  (let [lrepl-ids (filepath->lrepl-ids conf* filepath)]
    (into []
          (comp
           (filter (fn [[id data]] (not-empty (select-keys (:repls data) lrepl-ids))))
           (map (fn [[id data]] id))
           (filter (:tabs/active conf*)))
          (:tabs conf*))))

(defn lrepl-id->tab-ids
  [conf* lrepl-id]
  (let []
    (into []
          (comp
           (filter (fn [[id data]] (not-empty (select-keys (:repls data) [lrepl-id]))))
           (map (fn [[id data]] id))
           (filter (:tabs/active conf*)))
          (:tabs conf*))))



#_(defn dataize
    [conf]
    (walk/postwalk #(when (not (fn? %)) %)  conf))

(comment

  (re-matches  (re-pattern ".+.clj") "asd.clj")

  (require '[cljs.nodejs :as node])
  (defonce fs (node/require "fs"))
  (defonce path (node/require "path"))

  (def mult-edn-str
    (-> (.readFileSync fs "/home/user/code/mult/examples/fruits/.vscode/mult.edn") (.toString)))
  (def conf (read-string mult-edn-str))
  (def conf* (evaluate conf))

  (filepath->lrepl-ids conf* "/home/user/code/mult/examples/fruits/system/src/fruits/mango.clj")
  (filepath->lrepl-ids conf* "/home/user/code/mult/examples/fruits/system/src/fruits/banana.cljs")
  (filepath->tab-ids conf* "/home/user/code/mult/examples/fruits/system/src/fruits/mango.clj")
  (filepath->tab-ids conf* "/home/user/code/mult/examples/fruits/system/src/fruits/banana.cljs")
  
  ;;
  )