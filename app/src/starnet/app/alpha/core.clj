(ns starnet.app.alpha.core
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub]]
   [clojure.java.io :as io]))

(defn authorized?
  [{:keys {} :as channels}]
  (go
    
    )
  )

(defn create-token
  [{:keys [ch-access-store] :as channels}]
  (let []
    
    )
       :get (do (>! cout (.get token store))
                                           (recur store))
                                  :delete (do
                                            (>! ch-kproducer [["alpha.token" token
                                                               (fn [_ k ag]
                                                                 nil)] cout])
                                            (recur store))
                                  :update (do
                                            (>! ch-kproducer [["alpha.token" token
                                                               (fn [_ k ag]
                                                                 (update ag : )
                                                                 )] cout])
  )