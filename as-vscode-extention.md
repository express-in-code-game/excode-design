
# Death Star: search for Spok

## rethinking deathstar.ltee as vscode extension

#### understanding desktop

- latest advancement was realizing, that having a desktop app (ui), that can lauch server and nrepl as subprocesses, was the way to go
- it superseded the docker + browser system, because Death Star: laptop event edition should be a desktop app, in the spirit of simplicity and decentralization
- via plugins, deathstar.ltee can later acquire features of a volunteer cluster, but keeping the simple nature first

#### jvm or not jvm

- since it's desktop by nature, jvm seemed a better choice, given you can do ui with it
- jvm is simpler than node, because clj can be evaled direclty
- so if at all stages (UI, server, user game code files) Death Star uses one language - clojure - it is an enourmous simplification
- and so the decision was made to use jvm and javafx for gui
- however, javafx is no match for web ui and npm world, it's just less used
- browser technology (and nodejs electron belong to this category as well) is friendly to openness and epecially ui
- vscode is an example
    - compared to IntelliJ, whcih is heavy visually and clunky when you want to edit settings/plugins, vscode is simple, because file is in the center
    - settings.json is a file, openeing a directory is simple, extensions are many
    - vscode feels spacious and non-enforcing
    - why ? becuase it was designed by one of the developers of Eclipse (see youtube talks), who seeked simplicity and lightness
- vscode is the browser of editors
    - browser is probaly the most used desktop app in the world, simple, lightweight, open; just tabs
    - vscode is literally a browser (electron is node + browser) and design-wise is like browser: alomost everything is a tab
    - vsode is the continuation of the idea of a browser, a step forward advancing the browser into an editor
- the problem is nodejs, clojurescript being more complecated than clojrue (it's by design unavoidable - as cljs compiles to js)
- so on the one hand: you want electron (because browser tabs) and web (ui) tech, as it's graphics friendly and simple and super powerful
- on the other hand, you want jvm, because its clojure, and because nodejs is undesirable, and becuase multithreading is sane and comes out of the box with core.async
- on jvm, you can start a server subprocess with ease on the same jvm isntance and be sure all cores/threads will be utilized
- on node, you'll start a child_process, which is ok, but a bit more complex
- so, what is needed is this: either clojure, or clojurescript, but not both
- if you go jvm, then no web tech, no ui tools (ui will be a pain)
- if you go cljs, you have to figure out how to compile (and possible expose an nrepl) without start jvm-based tools; and you have to run nodejs based server (as child process), no pedestal
- although it seems a difficult choice to make, I would argue
    - if it is possible to figure out, how to have cljs eveywhere, being able to compile and provide nrepl, without starting jvm, npm world is better and friendlier to the game
    - tools and graphics and vscode (electron), browser make making ui simpler, it's a much richer enviroment (thx to cljs of course)
- so cljs everywhere would be preferable, can it be done ? it seems it can


#### deathstar.ltee as vscode extension

- vscode tabs are already subprocesses (browser tabs essentially), which can be great for Death Star: a tab can be a resource or solution space
- Death Star main tab can be like home page
- if you click "scenarios" for example, another tab (or same, but new view) can be opened, with the list of scenarios
- where will they come from ? repo!
    - R E P O
    - what if your Death Star game space was a repo ?
    - it would contain your settings files (game config, list of scenarios to download, games files in dir)
    - so if you start on a new machine and install vscode + Death Star, you can clone game repo and you are set to go, including your previous games code
    - and it's up to you what to keep; and you can always within the game press "generate a fresh repo" or other files
- so when you open that repo, Death Star will load configs, and open tabs; you can host or connect
- then, in the same editor, you can edit files
- and simulations can run in fresh tabs (each providing an independent enviroment for the player to experiment, even can be new on each simmulation)
- so tabs, tabs, tabs
- brilliant
- using vscode as a desktop user app is perfect
    - it's a living used-by-many system, constantly moving forward
    - it's lightweight and spacious
    - distribution, releases is done, all is needed is to release compiled Death Star extension (as one archive) to be installed from source (can even do without ext store)
    - all key abstractions are already in place: graphics, window resizing and other behavior, tabs, other extensions, editor etc.
    - when thinking about doing Death Star ltee on jvm, all those came to mind as the first thing to do; but with vscode, you start working on the value of the game immediately

#### nrepl

- to not rush to any conlusions, it's unclear exactly how to do, but cljs-everywhre is doable
- cljs compiler and state need to be understood, and how to create new compiler sessions etc.
- is nrepl server even needed ? can files be slurped and evaled via compiler isntance without running a dev tool ? the probably can, how can be figured out
- the goal is to either have a new cljs-tool that would provide nrepl (maybe Socker REPL will do? ) or not use nrepl at all (most likely)
- need to look into what Socket repl is and make cljs-everywhere work
- boom, beatch


#### or: vscode + background jvm process

- vscode starts and extension works as it does
- but it starts a background jvm for whatever can only be done in clojure
    - for example, cljs compiler is written in clojure, so to have cljs-in-cljs you need to get compiler state somewhere
    - OTOH, you can always use the extension's compiler state (or slection of namespaces)
- so
    - preferably, cljs only
    - if not, it's plasible to have a background jvm process to handle certain operations (f memory either way, only value matters)
- jvm process can be run in docker
- the question of installation: waht user needs to have
    - if vsocde only, it's perfect
    - if jvm background process
        - if it's jpackaged binary, it would have a large size within game release arhive
        - if it's uberjar, JRE (or JDK) will be required
        - if it's Dockerfile (or docker image), docker will be required


#### repls and tabs

- an example state of the editor when playing the game
    - left side has explorer open (game repo), and a {current-game-hash}.cljs file tab
    - right side is split into two: upper tab is solution space, bottom tab is resource space
- player can with ease connect and reconnect into the apps running inside resource and soulition tabs (which are isolated, real browser tabs)
- when connected to, say, solution, space, player can eval things and see the immediate result in solution space
- if brekage happens, player can press "reset" button (or (reset) in the repl) and solution space restores to the intial state
- tab is an evaluation environment, a runtime in which player runs their evalutaions, experiments (and graphically see changes)
- after the scenario generates resource and solution space, cljs compiler states are created for each - those keep the true state of resource and solution space
- when repl is given to the player, a copy of compiler state is created and exposed via nrepl/socket repl etc.
- if the player "breaks" that copy or discard the tab, they with one click can get another copy from source state
- during game simulation states are synced over network, and if all goes well, solution and resource space acquire new state
- so player if free to get fresh tabs/repls, change reosurce and soltuion space (for example, when changing a file, each space has it's own generated file/ns)


#### understanding nrepl, clj repl, cljs repl and shadow-cljs: Death Star tabs are like shadow-cljs builds

- steps of evaluating cljs code via extension (shadow-cljs example)
    - run multiple builds on shadow's jvm: :build1 :build2 :build3
    - open 3 tabs in the browser for each build
    - each tab has code injected by sahdow: it will connect to a certain host:port to recieve js for evaluation and send back data or error
    - each build connects to shadow websocket server for builds (so 3 conns are held)
    - each build has its own compiler state
        - unlike clojuscript itself (or nrepl piggiback), shadow does not use cljs.repl 
        - clojruescript compiles (on jvm) to js string, than it goes over a browser connection (part of cljs repl) to eval js and gets back result
        - shadow only uses cljs to compile js, but does the go-eval-js step itself: because it supports multiple builds
    - extension connects to nrepl server (hosted on jvm by shadow)
    - eval expression, send nrepl {:op :eval :code ""}
    - nrepl on jvm receives :op
    - shadow intercepts 
    - if it is a special shaodow :op "change build", it updates its :active-build state
    - if its an eval :op for :build2
        - shadow uses :build2 compiler state to get js string
        - then it goes over :build2 broser tab connection to eval js and gets back error or data
        - then it gives back result to nrepl
        - nrepl returns it to extension
    - so
        - cljs repl has one connection
        - shadow has multiple builds, multiple js-execution-enviroment connections, but one nrepl connection
        - shadow has additional nrepl :ops to select build
- given the explantion above
    - for Death Star, each tab is similar to build: tab is a js execution enviroment, tabs are multiple
    - yet, clj(s) extension should have one nrepl-protocol-compatible connection, that similar to shadow can be programmatically switched based on namespace for example
    - example
        - we have an nrepl-compatible server hosted on node (inside vscode)
        - plus, a hub (build like process) on top to swtich tabs given incoming nrepl data (and it holds connections to tabs)
        - we connect from clj(s) extension (mult)
        - given the namespace (resource or solutino space), extension sends :select-tab kind of messages to nrepl
        - build-like process (similar to sahdow) intercepts, selects a tab
        - next eval :ops are compiler using compiler state for corresponding tab and evaled over tab connections
    - so it's similar shadow, execpt in self-hosted env we have analyzer cache that is used for initial compiler states


#### how to approach creating a multiple-tabs-nrepl environment for the game ?

- jvm is not the issue: it's a different kind of functionality needed for the game, shadow-cljs does not cut it
- first, it should be built game specific, and from that a generic abstraction (tool) may appear
- how it would look like
    - the key is to expose nrepl protocol
    - the rest would be game specfic processes providing compiler states, tab connections and tab evaluations
    - tab app for solution space and resource space would have a process that would have an :eval op (of js code)
    - the other parts of the mechanism would be part of the extension runtime, doing necessary switching and compiling cljs to js, sending to tabs
- game specific, execpt maybe for nrepl-protocol server
- done this way, any extension would be able to connect 



#### thinking nodejs

- what apps deathstar builds
    - extension itself
    - generic tabapp
        - has self hosted cljs
        - visually empty, but has reagent ui (one settings cog) with Death Star extension specific ops
        - communicates with extension via channels ( has process(s) running )
        - exposes API for a sceanrio's app
        - once scenario code is loaded at runtime, it's tabapp's code is sent to generic tabapp and is evaled there, which starts scenario process, which uses generic tab's api
        - player gets a REPL into the tabapp
    - generic worker
        - runs in isolation sceanrio's generation api and other logic (that does not belong to tabapp)
        - communicates with extensions via channels
    - server
        - does player websocket connections
        - stores games and possibly game state
        - is a child_process node, that itself may spawn worker threads
    - libs
        - abstraction that implements nrepl (at its core at least), so player could eval into tabapp
    - deathstar ui tabapp
        - Death Star interface app
- how tabapp becomes a scenario
    - ~~Death Star keeps state of a running scenario (later will be persisted on disk/db)~~
        - nah, it is done on the server
    - extension starts scenario worker and tabapp, sends scenario code to both, cide is evaled, processes are started
- generation
    - scenario geenrates both data (that is used to run the solution space and repsurce space ui) and code - to output files for the user
- deathstar uses sceanrio's api to generate or apply generation
    - Death Star uses scenario's worker api to generate data/code and keeps it in memory - if tab crashes, it will be recreated with same generated data

- server vs exntension: who evals scenario and starts wokers, generates data, persist games?
    - if possible: start vscode headless instance to get vscode.api within the server (for file access and such)?


### server conundrum: nodejs or jdk, again

- since the system requires a standlaone server, the question arises: why node? or why jdk?
- why node
    - distribution: you only need to install vscode, where vscode runs, game can run
    - but it's kind of bs: as this game is definately for laptops/desktops, so running a jvm is not an issue
    - one language - cljs
- why jvm
    - it has tooling (for example, nrepl with cider can be used, with custom middleware for the game)
- JDK vs JRE
    - unknown, there is a possiblity that JRE does not cut it
    - either way, it's a jpackage question
- how to distrbute jvm
    - if possible: jpackage a JDK (or JRE if it suffices, depends on tools)
    - if possible: release it on github ? ~400mb? doc says yes : https://docs.github.com/en/github/managing-large-files/distributing-large-binaries
    - user launches vscode (and they don't need the server if they won't be hosting)
    - if they want to host, that press a button and extension downloads the binary and spawns a jvm child_process
    - so it's the extension that would download the server on demand (and store it somewhre deathstar related, like ~/deathstar)
- why is it the same as node
    - server is server, and communication will be over http or websockets
    - and it needs to store games and lauch db and such
    - you still need to do extension-server communictaion, and that negates the runtime question: it does not matter jvm or nodejs
    - bottom line: nodejs is not simpler, faster to implement; it's the same; then, the only questions are tooling and distribution
- why docker at first is fine and benefitial
    - no need to mess with jpackage at first, installing docker to hsot is perfectly fine at first (standalone will be added later)
    - lauch code will change a bit, but http/socket communication will stay
    - it's either jpackaged biinary on github.releases or docker image on docker hub
    - docker is fine to start with

#### beyond jvm just for server: vscode + jvm

- if only host runs jvm, there is no way to leverage tooling such as nrepl
- so the first-step solution (before these abstraction can be run on nodejs for example) is to run both vscode and jvm and distribute operations accordingly
- and it still possible to keep it unnoticable, as jvm can be jpackeged and background downloaded/updated by extension
- memeory is not a concern, at all, only value is
- vscode + jvm on each machine allows both to be host and leverage all the tooling available
- yet, overtime abstraction may migrate to a different runtime


#### vscode + jvm design


- vscode extension
- vscode tabapp to run scenario
- jvm instance running in docker
    - worker (for local ops)
    - server
- use sockets as they are channels
    - 1 socket for extension:jvm-worker channel
    - 1 socker for extension:server channel
- sync state
    - worker holds state of the game (tabapp, generation)
    - this state is synced between server and players
- evaluation
    - clj ext connects to shadow nrepl
    - intercept incomming evals (or results) to change the state etc.
        - prgrammatically (usig shadow api or intercept) re-eval changes inside the tabapp to bring it to any state
- tabapp
    - has menu of ops: reset to game state, reset to initial state etc.
- worker and server
    - worker connects to server generically (via socket) even though they may be on the same jvm
- automation
    - game creates files in game dir (~/deathstar for example)
    - when scenario starts, prtcly open the text ediotr tab for that file and connect to nrepl (if possible)
- multiplayer: design done right
    - games should be not for 2, but for 1 or more by design (to avoid pitfails): from the start, there can be 1 or many players, period
- extension ->socket-> worker ->socket-> server
    - like that


#### state: it's not about a repl into tab, it's about runtime-less language, data, state

- competetive game
    - the goal of the game are events, players playing and competing
    - for any game to be competetive, game state should be syncronized and be independent of player's actions (at least consensus)
- understanding vscode tab: you cannot sync state or advance it
    - when vscode tab goes into background, it suspends even message passing
    - say multiple players are playing, how would it look like
        - say, every tab can eval code
        - when it's opened, it's always starts at 0, initial state
        - ok, you send data and apply it to state
        - but players may be closing tabs or suspending them, so where is the source of truth? where does the game state run ?
        - nodejs: not an option, as you cannot create a background tab and give player REPl into to it (only real tabs, but they are suspendable)
        - jvm: there is no realistic way to run, say, a headless browser and use it's tabs as source of truth runtime - it's insane and not achievable
        - so when a simulation runs (palyers code in action), somewhere a source of truth (state) must be formed
        - and with tab-REPL approach it is only possible to rely on an open tab somewhere to run the whole simulation and get the state and then sync it; that's not suitable for competetive game
- problems with REPL, tab, self hosting and tools
    - in short: you cannot simply give a player a REPL into a js environemt (isolated tab,so they can crash it and reload)
    - shadow-cljs is not usable programmatically and it's fair: it's not designed for it
    - figwheel is not a solution either - simpler to use clojurescript compiler with async, new :bundle taregt and webpack
    - building from sratch a cljs build tool (sol) would be cool, but it does not eventually solve the non-stable nature of vscode tabs
- state, it's about state
    - what is the solution then ? palyers still need a REPL , but game state should be advancable, recreatable, syncable and independent of ui 
    - data, data, data: advancing (changing) state approach
        - players eval code, but this code should be clojure common - runtime independent
        - this code is about data, it has no sideeffects (only produces data that will be used for rendering sideeffects)
        - this code changes state or prvides logic functions, processes, channels - things that are runtime independent
        - players are runtime independent!
        - what the eval, can be run on jvm, nodejs, in the browser or elswhere
        - data, state, logic, processes, channels
- so what is vscode tab then ? what is sceanrio then ?
    - vscode tab is a renderer: a sideeffect, that should be disposable and should not affect the state of the game; and it is
    - scenario with runtime-independent approach will consist of two parts
        - cljs specific application that renders
        - runtime-independent code for advacing state and generating data
        - runtime-independent process (!!! yes, with core async) that provides game-logic api: who wins/loses depending on state and data
- players evalution, networking and synchronyzation
    - we have both runtimes (all three actually): vsocde extension runs a jvm worker, which hosts a server
    - game state resides on the server and is synced also to every worker (so each player's jvm has game state)
    - when scenario is loaded, it's generic code is used on jvm and render-app runs in vscode tab
    - worker has nrepl running
    - when palyer evals code in a file (cljc file, code that can be run on any runtime)
        - it goes over nrepl (although it does not matter, but it is treated as clj - because tooling is simpler)
        - it arrives to jvm and is stored there (as player's event, for replayablity)
        - it also is evaled and applied to state (scenario's code is used for that), then this game state is synced with server and all other players
        - worker gives back to nrepl a result of that evaluation (some game data, it is always data)
        - player sees the result in the REPL
        - so the only difference from the usual approach is that evaluation is runtime-less (but language comes as is, complete) and is within game's api and data 
- worker/server (the Death Star game, these module will run on both) keep state (data) and advance/replay etc.
- language, data, state

- sending code or state changes
    - code first (see what's what), or state if needed
- how to def: namespaces are free
    - on the worker, a player will get their namespace (so the can def as much as needed)
    - namespaces can be discarded and re-created
- what tab is
    - tab is a glorified renderer over channel