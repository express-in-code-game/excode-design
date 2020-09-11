
- build a vscode extension
- use deathstar and cljctools namespaces

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

## exlpicit channel mappings in main

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


## conveying values from multiple channels in one runtime over socket onto multiple channels in another runtime

- there should be a non-generic process that takes from local channels and puts over socket, adding ::channel-key-word| (which channel to use on the other side)
- on the other side proc takes and puts to a channel using that ::channel-key-word
- this will allow app to be flexible and no necesseriily map key-to-key (e.g. you can take from 2 chans here, but send to one ::some-chan| on the other side)
- the problem is :out| channels: how do you keep using put-back channels over socket? or should you ? maybe it's jsut bad design to be in such situation to begin with


## problem with nrepl

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
  

## local and shared (system) specs

- there are local specs and one system specs
- local specs may depend on system spec to use kewords like ::core.spec/some-system-wide-op and make their op and vl macros validate against shared ops/vals
- local spec has it's channel keys, and using sytem spec insde local vl macro basically says "hey, want to know at compile time that this local channels handles this shared op"
- processes import multiple specs: one (local), two (local, shared) or more

## understaning store, state, hub and opeartions

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


## impl.api and chan.api: the approach to channels and values with having a programmatic impl-independent runtime-independent api

- create-channels should be in a separate ns  (and dep) from implementaion (create-proc)
- lets call it chan_api
- example functions
  - create-channels
  - connect
  - disconnect
  - ...
- chan_api only hus runtime-independent dependencies: meta (spec, protocols) and core.async
- core.async is part of the language (luxury to have it as a lib), so it is not a dep, it's core
- foo.chan_api requires example
  - [ core.async ]
  - [ foo.spec ]
- and it can be imported in any runtime along with meta (chan_api may be part of meta)
- lets say, foo.impl runs on nodejs, and bar runs on jvm
  - bar.impl is abstract, it does not know or care that it runs or jvm
  - but what it knows, is foo.spec and foo.chan_api
  - it imports those as a dep (foo/meta)
  - so bar.impl requires
    - [ foo.chan_api ]
    - [ foo.spec ]
  - bar.impl has create-proc-ops, that gets channels as arguments
    - some of those channels are foo's :   ::foo.spec/one| ::foo.spec/two|
    - but: bar does not know or care, if foo is over socket or http or in the same runtime
    - who does ? only bar.main, which creates channels and directs values from socket to that channel
  - now: if bar.impl and foo.impl must run in the same runtime, and for whatever reason bar.impl needs foo.impl ref, bar.impl can (require [ foo.impl ]) explicitely
  - so in 99% of the time, bar requires only channels
    - [ foo.spec ]
    - [ foo.chan_api ]
  - and in 1% of the time (rare optimization case or whatever)
    - [ foo.impl ]
    - and foo isntance as passed as explicit arg to proc-ops (as it's not standard) and should be explicit
- what foo.chan_api looks like

```clojure

foo.chan_api

(defn connect 
"Instead of instance, I take channels as first arg"
[channels opts]
(let [out| (chan 1)]
(put! (::foo.spec/some-chan| channels) {::csp.sepc/op ::foo.spec/connect :xyz opts ::csp.spec/out| out| })
out|
)

)

bar.impl

(go (loop []

...
(let [data (<! (foo.chan_api/connect {::foo.spec/some-data 123} )) ]

)

))



```

- this api can represent abstractions anywhwere, no clash of dependencies
- so a jvm runtime can import foo/meta and use programmatic api, yet values are conveyed over channels
- but code is a bit more concise, having functional api makes it more abstarct while 
  - keeping async nature (it runtime-less, network-or-not independent)
  - kepping meta (spec protocols chan_api) freely imporatble : meta can be importated by any dep, any runtime


## out| channel over network

- lets take http example
- bar.impl calls (<! (foo.chan_api/some-reqest channels data))
- foo.chan_api adds out| channel
- bar.main behind the scenes redirects values from foo's channels to http proc
- http proc takes that value, does dissoc :out|, sends data-only value over network and immediately does (take! http-request (fn [resp-from-network] ( put! onto the original out| ))
- on the other side, proc adds out| and immediately does (take! (fn [v] send-response-over-network))
- so before leaving runtime, out| is dissoced, when response arrives it is put onto that out|

## closing channels: auto-close-on-first-take channel would be great

- the operation initiator calling  should decide what out| is and when it should be closed
- if its a system channel, it should not be closed at all, and you don't use <! blocking take
- but if you (<! (foo.chan_api/some-op channels data out|)) you should also close the out|
- what is preferable is this
  - (<! (foo.chan_api/some-op channels data (one-time-autoclose-on-take-channel) ))
  - so the proc cretes such a channel, and after first take completes, channel is disposed

- channels will be garbage collected even if not closed ?
  - https://stackoverflow.com/a/28889316/10589291

## yes, channels are just instances, if there are no puts/takes, they will be garbage collected

- https://github.com/clojure/core.async/blob/master/src/main/clojure/clojure/core/async/impl/channels.clj
- so closing indeed is to be used for meaning, but not for garbage colelction
- closing will be used for close| or release|, or for merge etc.
- when needed


## channel api (hub.chan, remote.chan): applying to remote->hub requests/operations

- once again, what is channel api
  - abstraction (namespace) consists of at least two deps
    - project.foo/impl 
      - contains
        - project.foo.impl 
        - project.foo.xyz.impl 
        - ...
      - imports
        - project.foo.spec :as foo.spec
        - project.foo.chan :as foo.chan
      - uses
        - ::foo.chan/ops| 
        - (foo.chan/connect channels opts)
        - ::foo.spec/host ::foo.spec/port ... 
    - project.foo/meta
      - contains 
        - project.foo.spec
        - project.foo.protocols
        - project.foo.chan
          - imports
            - project.foo.spec :as foo.spec
          - exports
            - operations/values specs
            - create-channels
            - asynchronos funcitonal api for the implemtation process: (connect channels opts) (disconnect channels opts) ...
- this allows to use the same functional asynchronous api both inside foo.impl and in any other part of the system, which talks to foo.impl
- foo.chan makes code concise and explcit: 
  - you can always do (<! (foo.chan/connect channels)) - one line, because connect will add out| if needed
  - and functional api is explicit argumentwise
- foo.chan absolute alloows you (in that rare case) use foo.api or foo.impl directly, synchornously, access an instance
- but 99% percent of data flow in a project is asynchronous
- even better: hub.chan allows remote.impl to talk to hub as if they are in the same runtime, without any code changes
  - processes are mostly abstract,runtime-less
  - go-blocks are designed for parallelsim (you can always (doseq [n (range 0 16)] (go (loop [] (<! same-channel for 16 procs for parallelsim ))
  - this means, that when remote code calls (hub.chan/join-game channels opts) it is agnostic to where hub is, because hub is just channels
  - what happens is, the remote's runtime (main)  will import hub.meta and own remote.meta and will call the same (hub.chan/create-channels as hub itself does)
  - so on the server (clj), hub has called (hub.chan/create-channels) and on the remote (different runtime, cljs) main also called (hub.chan/create-channels)
  - so both hub and remote have the same set of channels
  - remote.impl (process) imports hub.chan and gets local cahnnels as agruments
  - it uses (hub.chan/join-game) (hub.chan/leave-game) - the channel api provided by hub.meta
  - since channels mirror actual hubs channels, remote's runtime (in the main) would pipeline values from local hubs channels to be converted to network requests and be put back on out| channels used by remote.impl (those out| are mostly created by hub.chan itself)
  - so
    - remote calls parking (<! (hub.api/join-game channels opts))
    - the value was put! on local ::hub.chan/ops| 
    - main has pipelined values from ::hub.chan/ops|  to be put on ::local/http| that makes request to hub
    - before request, out| is dossoc, but used inside a callback  - when response arrives it is put on out|
    - so remote used a hub's op over network, but abstractions are written as if they all work in one runtime via core.async abstraction

## channel api taps (e.g. hub.tab.remote ): tap into chan-api channel traffic to form a derived state, without altering operations

- hub performs operaions by tapping into channels created by hub.chan api
- it forms state and  - it forms responses to those operations
- on the user side, hub abstraction requires a remote state (for rendering and other purposes)
- first, there is a wrong approach of making a remote a sepearte abstracion from hub, with it's own operations
  - problem is, that remotes ops are exactly hub ops, so it one-two one duplication, only state is differernt (hub has all games, remote will have user's games etc.)
- what is needed is to decouple forming that state from using hub.chan api directly
- so there is no remote: there is hub.impl and hub.chan api for it, hub.chan api is async and runtime independent
- but where to form that remote-specific state ? it's different from hubs
- what we want
  - to start remotes in any runtime like this
    - (def channels1 (hub.chan/create-channels))
    - (def channels2 (hub.chan/create-channels))
    - (def remote-state1 (hub.tap.remote/create-proc-ops channels1))
    - (def remote-state2 (hub.tap.remote/create-proc-ops channels2))
  - perform operations using  hub.chan api directly, unchanged or specific
    - (hub.chan/op {:some-op} channels1)
    - (hub.chan/op {:some-op} channels2)
  - redirect trafic from channels1 and channels2 to be put onto socket or local hub's channels
    - so that the same hub processes channels1 and channels2 values, but their requests/responses are different (each represents a user)
  - and
    - make it possible for remote-state1 and remote-state2 , given hub.chan api's cahnnels, to intercept those operations and responses 
    - and form some local state specific for hub.tap.remote abstraction (process)
    - e.g. their can be hub.tap.remote1 hub.tap.remote2 etc.
- with this approach
  - abstraction has a channel api, that both can be used by abstraction itself or elswehre (by a different process)
  - and yet, there can be a derived (from tapping into trafic) state, formed anywhere by passing abstraction's channels
  - example of hub and remote
    - extenstion is remote, it will use hub.chan/op  api to join-game, leave-game, list-users etc.
    - it will redirect values from hub.chan channels into socket/http-request (on the server side, the go into hub process and back)
    - it will instantiate independent, completely decoupled remote-state (hub.tap.remote/create-state) and (hub.tap.remote/create-proc-ops channels remote-state)
    - the hub.tap.remote process will intercept the traffic on hub's channels and form (swap) the state
    - and extension can add-watch to that state or use it any other way


