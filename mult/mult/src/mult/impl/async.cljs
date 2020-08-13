;;   Copyright (c) Rich Hickey and contributors. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns mult.impl.async
  (:require
   [clojure.core.async :as a :refer [<! >!  chan go alt! take! put! offer! poll! alts! sub mult tap untap
                                     timeout close! to-chan go-loop sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]))

; why
; needed a way to close a topic's underlying mult

(defprotocol Topic
  (close-topic* [_ topic]))

(defn close-topic [pub topic]
  (close-topic* pub topic))

; code from clojure/core.async https://github.com/clojure/core.async/blob/master/src/main/clojure/clojure/core/async.clj#L881

(defn pub
  "Creates and returns a pub(lication) of the supplied channel,
  partitioned into topics by the topic-fn. topic-fn will be applied to
  each value on the channel and the result will determine the 'topic'
  on which that value will be put. Channels can be subscribed to
  receive copies of topics using 'sub', and unsubscribed using
  'unsub'. Each topic will be handled by an internal mult on a
  dedicated channel. By default these internal channels are
  unbuffered, but a buf-fn can be supplied which, given a topic,
  creates a buffer with desired properties.
  Each item is distributed to all subs in parallel and synchronously,
  i.e. each sub must accept before the next item is distributed. Use
  buffering/windowing to prevent slow subs from holding up the pub.
  Items received when there are no matching subs get dropped.
  Note that if buf-fns are used then each topic is handled
  asynchronously, i.e. if a channel is subscribed to more than one
  topic it should not expect them to be interleaved identically with
  the source."
  ([ch topic-fn] (pub ch topic-fn (constantly nil)))
  ([ch topic-fn buf-fn]
   (let [mults (atom {}) ;;topic->mult
         ensure-mult (fn [topic]
                       (or (get @mults topic)
                           (get (swap! mults
                                       #(if (% topic) % (assoc % topic (mult (chan (buf-fn topic))))))
                                topic)))
         p (reify
             a/Mux
             (a/muxch* [_] ch)

             a/Pub
             (a/sub* [p topic ch close?]
               (let [m (ensure-mult topic)]
                 (tap m ch close?)))
             (a/unsub* [p topic ch]
               (when-let [m (get @mults topic)]
                 (untap m ch)))
             (a/unsub-all* [_] (reset! mults {}))
             (a/unsub-all* [_ topic] (swap! mults dissoc topic))
             Topic
             (close-topic* [p topic]
               (when-let [m (get @mults topic)]
                 (close! (a/muxch* m))
                 (swap! mults dissoc topic))))]
     (go-loop []
       (let [val (<! ch)]
         (if (nil? val)
           (doseq [m (vals @mults)]
             (close! (a/muxch* m)))
           (let [topic (topic-fn val)
                 m (get @mults topic)]
             (when m
               (when-not (>! (a/muxch* m) val)
                 (swap! mults dissoc topic)))
             (recur)))))
     p)))

