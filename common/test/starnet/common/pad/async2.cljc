(ns starnet.common.pad.async2
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub]]))


(defn main-process
  [p]
  (let [c (chan 1)]
    (sub p :core c)
    (go
      (loop []
        (when-let [[[t v] c] (alts! [c])]
          (condp = v
            :up (do
                  (println "system is up")
                  (recur))
            :check (do
                     (println "system is ok")
                     (recur))
            :down (do
                    (println "system is down")
                    (recur))
            :close (do
                     (println "main process will close")
                     (a/close! c)))))
      (println "main-process exiting"))
    c))

(comment

  (def system-chan (chan (a/sliding-buffer 10)))
  (def system-chan-publication (pub system-chan first))

  (def c (main-process system-chan-publication))

  (put! system-chan [:core :up])
  (put! system-chan [:core :check])
  (put! system-chan [:core :down])
  (put! system-chan [:core :up])
  (put! system-chan [:core :close])

  (a/close! c)

  ;;
  )


(comment


  (def c (go
           (do
             (<! (timeout 1000))
             (println "done"))
           [1 2]))

  (take! c (fn [v] (println v)))


  (def c (go
           (let [c (chan 1)]
             (do
               (<! (timeout 1000))
               (println "done")
               (>! c [1 2])
               c))))
  
  (take! c (fn [v] (println v)))

  ;;
  )


(comment
  
  (<!! (go
         (<! (go
               (<! (timeout 1000))
               3))))
  
  ;;
  )


(comment


  (defn proc1
    [c]
    (go (loop [cnt 0]
          (if-let [v (<! c)]
            (println (format "proc1: val %s cnt %s" v cnt)))
          (recur (inc cnt)))
        (println "proc1 exiting")))

  (def c1 (chan (a/sliding-buffer 10)))

  (def p1 (proc1 c1))
  (put! c1 {})

  (a/poll! c1)

  (a/close! p1)
  (a/close! c1)


  ;;
  )