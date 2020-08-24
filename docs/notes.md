
- build a vscode extension
- use deathstar and cljctools namespaces

## experiments

- go w/o shadow-cljs? possible ? w/ cljs build only ?

## (:require-macros [])

- https://cljs.github.io/api/cljs.core/defmacro
- https://clojurescript.org/guides/ns-forms
- https://code.thheller.com/blog/shadow-cljs/2019/10/12/clojurescript-macros.html

- the way it's done with deathstar.core.spec
  - deathstar.core.spec.cljc is one file that is both macro ns and runtime ns
  - it requires itself using reader conditional  #?(:cljs (:require-macros [deathstar.core.spec]))
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
  

#### local and shared (system) specs

- there are local specs and one system specs
- local specs may depend on system spec to use kewords like ::core.spec/some-system-wide-op and make their op and vl macros validate against shared ops/vals
- local spec has it's channel keys, and using sytem spec insde local vl macro basically says "hey, want to know at compile time that this local channels handles this shared op"
- processes import multiple specs: one (local), two (local, shared) or more

#### understaning store, state, hub and opeartions

- state is more abstract, store is something hub uses to read/write data
- hub parallelsim
  - hub creates specified (e.g. 16) go-blocks, that will in parallel take! from a single request/op channel
  - processing ops within a process is much better - you define local scoepe (channels, ctx) once, have ::specced operations with macroexpansion, no unncessary fn names for each opearion (keywords are better)
- store
  - trying to implement store as ops like ::add-user ::remove-user etc. is wrong and reduntant - almost the same ops are already in hub process (which already handles ::user-connected ::list-users)
  - what you want is to separate the store abstraction from implemtnation
  - but: it is a mistake (redundant) to re-map same opearions in another process, invent you pseudo-query/txn language (just a form of rest)
  - you want a store with a query language and use it directly in the hub - this way ops are handled properly in one place, with the same parallesim
  - however, the problem is that you want the same hub - written in cljc with those queries - to work with in-memory db and with disk-db 
  - hub will still import abstract.store.api ns, but it should have two implemntaions, that can be switched in deps.edn
  - you want your hub to work as a generic abstractions, handling ops, making read/writes/decisions and conveying back responses (via out channels)
  - if you take store (db) that is only disk or ony memeory (or if their api is different), you would endup needing to implement hub twice, which is out of the question
  - yes, your own store is some kind of a solution, that will (via enourmous duplication) make hub generic, whereas store substitutable