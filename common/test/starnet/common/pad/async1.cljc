(ns starnet.common.pad.async1
  (:require
   [clojure.core.async :as a]))

; https://github.com/clojure/core.async/wiki/Pub-Sub

(comment

  (def input-chan (a/chan))
  (def our-publication (a/pub input-chan :msg-type))
  (a/>!! input-chan {:msg-type :greeting :text "hello"})

  (def output-chan (a/chan))
  (a/sub our-publication :greeting output-chan)

  (def l (a/go-loop []
           (let [{:keys [text]} (a/<! output-chan)]
             (println (str "msg: " text))
             (recur))))

  (a/>!! input-chan {:msg-type :greeting :text "hello"})

  (let [c (a/chan)]
    (a/sub our-publication :greeting c)
    (a/go-loop []
      (let [{:keys [msg-type text]} (a/<! c)]
        (println (str "chan2 msg: " text))
        (recur))))

  (def loser-chan (a/chan))
  (a/sub our-publication :loser loser-chan)
  (a/>!! input-chan {:msg-type :loser :text "I won't be accepted"})

  (a/put! input-chan {:msg-type :greeting :text "hello2"})
  
  

  ;;
  )