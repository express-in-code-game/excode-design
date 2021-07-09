(ns pad.async1
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [is run-all-tests testing deftest run-tests]]))

(comment

  (def c1| (chan 1))
  (def c2| (chan 2))
  (def cs| (a/map + [c1| c2|]))

  (go-loop []
    (let [vs (<! cs|)]
      (println vs))
    (recur))

  (put! c1| 1)
  (put! c2| 2)

  (a/pipe (chan 1) (chan 1))

  (as-> 3 x
    (inc x)
    (do (prn x) (dec x))
    (prn x)
    x)


  ;;
  )

(comment
  
  ; exceptions in go bloack do not affect other go blocks

  (def c| (chan 1))

  (go
    (loop []
      (.println System/out (str "foo " (<! c|)))
      (throw (ex-info "error" {}))))

  (go
    (loop []
      (.println System/out (str "bar " (<! c|)))
      (recur)))

  (put! c| (rand-int 10))

  ;;
  )



(comment

  ; will alts capture both ops eventually?

  (def c1| (chan 1))
  (def c2| (chan 1))

  (put! c1| 1)
  (put! c2| 2)
  
  (go (loop []
        (when-let [[value port] (alts! [c1| c2|])]
          (println ::value value)
          (recur))))
  
  ; yes, obviously, it takes the first, loops, takes the second


  ;;
  )

