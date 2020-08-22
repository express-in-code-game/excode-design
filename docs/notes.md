
- build a vscode extension
- use deathstar and cljctools namespaces

## experiments

- go w/o shadow-cljs? possible ? w/ cljs build only ?

## (:require-macros [])

- https://cljs.github.io/api/cljs.core/defmacro
- https://clojurescript.org/guides/ns-forms
- https://code.thheller.com/blog/shadow-cljs/2019/10/12/clojurescript-macros.html

- the way it's done with deathstar.spec
  - deathstar.spec.cljc is one file that is both macro ns and runtime ns
  - it requires itself using reader conditional  #?(:cljs (:require-macros [deathstar.spec]))
  - why
    - because we need this whole ns at compile time (clojure time) and runtime (cljs)
  - how it works
    - simple: first file is read as .cljc for cljs
    - conditional returns (:require-macros) form, which tell compiler to look for macros
    - since ns is the same, it reads the same file again, but this time as macro ns (clj), reader conditional is skipped (so it does not error because :require-macros is cljs only)
  - what happens
    - when file is read as macros, it's evaled in clj during compilation and spec can be used during macroexpansion
    - when it's read as cljs file, it becomes part of runtime


## implementation design

#### exlpicit channel mappings in main

- use fully qualified names for channels, and destcturing in the proc that uses them
  - https://clojure.org/guides/destructuring#_namespaced_keywords
- if channel should come from another namespace, it should be passed as simple keyword in main, mapped from fully-qualified to simple
- in other words, proc knows its own channels and can destructure them. Other channel arguments are mapped explciitly in main and passsed as additional args

```clojure

(ns foo.api)

(defn create-channels
[]
(let [send| (chan 10)
      send|m (mult send|)
      recv| (chan 10)
      recv|m (mult recv|)]
;; use fully qualified keywords
{::send| send|
::recv| recv|
...
))

; other| is simple, will come from mapping
(defn create-proc-ops
[channels ctx opts]
(let [{:keys [::send| ::recv| other|]}  channels]
...
))

(ns app.main
(:require 
  [foo.api]
  [bar.api]
  [xyz.api]
))

; channels 

(def channels 
(let []
(merge 
(foo.api/create-channels)
(bar.api/create-channels)
(xyz.api/create-channels)
))

; map channel explcitely in main, keeping foo proc decoupled from bar.api ns (at least in terms of channels, may still neen bar.spec :as bar.sp  ,  bar.sp/op bar.sp/vl)
(def proc-foo (foo.api/create-proc-ops 
              (merge channels {:other| (::bar.api/send| channels ) })              
)

```

- unless (regarding importing bar.spec in foo.api)
  - instead, op and vl functions can be passed as args as well


#### conveying values from multiple channels in one runtime over socket onto multiple channels in another runtime

- there should be a non-generic process that takes from local channels and puts over socket, adding ::channel-key-word| (which channel to use on the other side)
- on the other side proc takes and puts to a channel using that ::channel-key-word
- this will allow app to be flexible and no necesseriily map key-to-key (e.g. you can take from 2 chans here, but send to one ::some-chan| on the other side)
- the problem is :out| channels: how do you keep using put-back channels over socket? or should you ? maybe it's jsut bad design to be in such situation to begin with


#### problem with nrepl

- what is needed
  - to intercept ops (:eval mainly) when it arrives, and before it leaves (should be able to access :value before it is sent back)
- what happens
  - nrepl has middleware
    - default https://github.com/nrepl/nrepl/blob/master/src/clojure/nrepl/server.clj#L85
    - cider https://github.com/clojure-emacs/cider-nrepl/blob/master/src/cider/nrepl.clj#L525
  - it you can add middleware to handle ops, and specify before/after
  - problem with msg from "eval" middleware
    - it does not have :value key
      - https://github.com/nrepl/nrepl/blob/master/src/clojure/nrepl/middleware/interruptible_eval.clj#L118
    - t/send sends it out directly ?
  - this is  transports send-fn, which flushes and socket-sends ?
    - https://github.com/nrepl/nrepl/blob/master/src/clojure/nrepl/transport.clj#L119
- what is the approach
  - fork nrepl
  - access value here https://github.com/nrepl/nrepl/blob/master/src/clojure/nrepl/middleware/interruptible_eval.clj#L118
  - put! it onto system's channels
- no, one more time:
  - first, nrepl middleware is an ugly wrap-pattern (high-order hell), 
  - some midllewares can short-circuit (unlike pedestal for examplem which passes ctx though each step)
  - so eval short-circuits
  - :requires and :expects only check that other middlewares are in place (in the list)
  - some middleware can call (handler msg), which is what's needed, but eval does not, shirt circuits instead
  - should nrepl be forked ?
    - no, if need be better so use (alter-var-root) and dynamically change it
    - or create a replacement that is nrepl-protocol-compatible 
  - is access to :value needed in the system
    - no
    - first, getting the eval code (not :value) is possible, so that makes it possible to replay evals
    - second, when player evals, they eval in their own namespace and change their state
    - so that namepsace and it's state (data) already represent player's state
    - in other words
      - what player sends to evals is needed (to replay)
      - players namespace and state (data, atom) is what is synced and exhanged
      - results of individual eval operations do not matter 
  