(ns mult.impl.lrepl
  (:require
   [clojure.core.async :as a :refer [<! >!  chan go alt! take! put! offer! poll! alts! pub sub unsub
                                     timeout close! to-chan  mult tap untap mix admix unmix
                                     pipeline pipeline-async go-loop sliding-buffer dropping-buffer]]
   [goog.string :refer [format]]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   [mult.protocols :as p]
   [mult.impl.channels :as channels]))


(defn lrepl-plain
  []
  (let [session (atom nil)]
    (reify
      p/Eval
      (-eval [_ conn code ns-sym]
             (let [code* (format
                          "(do (in-ns '%s) %s)" ns-sym code)]
               (go
                 (when-not @session
                   (when-let [{:keys [new-session] :as v} (<! (p/-clone-session conn))]
                     (reset! session new-session)))
                 (<! (p/-eval conn {:code code*
                                    :session @session
                                    :done-keys [:err :value]}))))))))

(comment
  
  (let [ns-sym 'foo.bar]
    `(in-ns ~ns-sym b c))
  ;;
  )

(defn lrepl-shadow-clj
  [opts]
  (let [session (atom nil)]
    (reify
      p/Eval
      (-eval [_ conn code ns-sym]
        (let [code* (format
                     "(do (in-ns '%s) %s)" ns-sym code)]
          (go
            (when-not @session
              (when-let [{:keys [new-session] :as v} (<! (p/-clone-session conn))]
                (reset! session new-session)))
            (<! (p/-eval conn {:code code*
                               :session @session
                               :done-keys [:err :value]}))))))))

(defn lrepl-shadow-cljs
  [{:keys [build]}]
  (let [session (atom nil)]
    (reify
      p/Eval
      (-eval [_ conn code ns-sym]
        (let [code0 (format "(shadow.cljs.devtools.api/nrepl-select %s)" build)
              code* (format
                     "(do (in-ns '%s) %s)" ns-sym code)
              code* (format
                     "(binding [*ns* (find-ns '%s)]
                      %s
                      )" ns-sym code)]
          (go
            (when-not @session
              (when-let [{:keys [new-session] :as v} (<! (p/-clone-session conn))]
                (reset! session new-session)))
            #_(<! (p/-eval conn {:code code0
                                 :session @session
                                 :done-keys [:err :value]}))
            (<! (p/-eval conn {:code code
                               :session @session
                               :done-keys [:err :value]}))))))))

(defn lrepl
  [{:keys [type runtime build] :as opts}]
  (cond
    (= [type] [:nrepl-server]) (lrepl-plain)
    (= [type runtime] [:shadow-cljs :cljs]) (lrepl-shadow-cljs opts)
    (= [type runtime] [:shadow-cljs :clj]) (lrepl-shadow-clj opts)
    :else (throw (ex-info "No lrepl for options " opts))))


(comment
  

  ;;
  )