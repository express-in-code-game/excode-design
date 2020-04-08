(ns starnet.pad.async2
  (:require
   [clojure.core.async :as a :refer [<! >! chan go alt! take! put! offer! poll! alts! to-chan
                                     timeout pub sub close! mult tap untap mix admix unmix
                                     sliding-buffer dropping-buffer
                                     go-loop pipeline pipeline-async]]))


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

  (defn proc2
    [c1 c2]
    (go (loop [cnt 0]
          (if-let [[v c] (alts! [c1 c2])]
            (println (format "proc2: val %s cnt %s" v cnt)))
          (recur (inc cnt)))
        (println "proc2 exiting")))

  (def c1 (chan (a/sliding-buffer 10)))
  (def c2 (chan (a/sliding-buffer 10)))

  (def p2 (proc2 c1 c2))
  (put! c1 :a)
  (put! c2 :b)

  (let [[v c] (alts!! [c1 c2 ])]
    (println v))
  
  (a/close! c1)

  ;;
  )

(comment
  
  (def c1 (chan 1))
  (def c2 (chan 1))
  
  (go (loop []
        (alt!
          c1 ([v] (println v) (recur))
          c2 ([{:keys [a b]}] (println [a b]) (recur)))))
  
  (put! c1 {:a 1 :b 2})
  
  (put! c2 {:a 1 :b 2})
  
  
  
  ;;
  )


(comment

  (def chan-close (chan 1))
  (go (loop []
        (alt!
          (timeout (+ 500 (rand-int 800))) (do
                                             (println "tout")
                                             (recur))
          chan-close (println "chan-close ")))
      (println "exiting loop"))

  (a/close! chan-close)
  ;;
  )


(comment
  
  (def pc (a/promise-chan ))
  (put! pc 3)
  (take! pc (fn [v] (println v)))
  
  ;;
  )


(comment

  (let [c (chan)]
    (offer! c 42))

  (let [c (chan 1)]
    (offer! c 42))

  ; pipeline

  (def c1 (chan 10))
  (def c2 (chan 10))

  (def _ (pipeline 4 c2 (map inc) c1))

  (doseq [i (range 10)]
    (put! c1 i))
  (go-loop []
    (println (<! c2))
    (recur))

  ; pipeline-async

  (def c1 (chan 10))
  (def c2 (chan 10))
  (def af (fn [input port]
            (go
              (<! (timeout (+ 500 (rand-int 1000))))
              (offer! port (inc input))
              (close! port))))

  (def _ (pipeline-async 4 c2 af c1))
  (doseq [i (range 10)]
    (put! c1 i))
  (go-loop []
    (println (<! c2))
    (recur))

  ; pipeline-blocking


  (time (let [blocking-operation (fn [arg] (do
                                             (<!! (timeout 1000))
                                             (inc arg)))
              concurrent 4
              output-chan (chan)
              input-coll (range 0 4)]
          (pipeline-blocking concurrent
                             output-chan
                             (map blocking-operation)
                             (to-chan input-coll))
          (count (<!! (a/into [] output-chan))))) ; ~ 1000 ms


  ;;
  )

(comment

  (def src| (chan 10))
  (def src|m (mult src|))
  (def src|p (pub (tap src|m (chan 10)) :topic (fn [_] (sliding-buffer 10))))
  (def c1| (sub src|p :abc (chan 1)))
  (def c2| (tap src|m (chan 1)))

  (go
    (loop []
      (let []
        (alt!
          c1| ([v] (println "c1" v))
          c2| ([v] (println "c2" v)))
        (recur))))

  (put! src| {:topic :abc :v 1})

  (go
    (loop []
      (if-let [xs (<! (a/map vector [c1| c2|]))]
        (println xs)
        (recur))))

  (put! src| {:topic :abc :v 1})

  (close! c1|)
  (close! c2|)

  (take! c1| (fn [v] (println v)))
  (take! c2| (fn [v] (println v)))




  ;;
  )

(comment

  (def out| (chan 10))

  (def out|mix (mix out|))

  (def c1| (chan 10))
  (def c2| (chan 10))
  (admix out|mix c1|)
  (admix out|mix c2|)

  (put! c1| 1)
  (put! c2| 1)

  (take! out| (fn [v] (println v)))


  ;;
  )

(comment
  
  (def source| (chan 1))
  (def source|m (mult source|) )
  
  (admix some|mix (tap source|m (chan 10)) )
  
  (close! source|)
  ; source closes
  ; tap closes
  ; mix removed?
  ; mult closed?
  
  
  ;;
  )

