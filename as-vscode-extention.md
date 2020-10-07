
# Death Star: search for Spok

rethinking Death Star laptop event edition as vscode extension

- [links to discussion and implementation](https://github.com/DeathStarGame/docs#links)

## understanding desktop

- latest advancement was realizing, that having a desktop app (ui), that can lauch server and nrepl as subprocesses, was the way to go
- it superseded the docker + browser system, because Death Star: laptop event edition should be a desktop app, in the spirit of simplicity and decentralization
- via plugins, deathstar.ltee can later acquire features of a volunteer cluster, but keeping the simple nature first

## jvm or not jvm

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


## deathstar.ltee as vscode extension

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

## nrepl

- to not rush to any conlusions, it's unclear exactly how to do, but cljs-everywhre is doable
- cljs compiler and state need to be understood, and how to create new compiler sessions etc.
- is nrepl server even needed ? can files be slurped and evaled via compiler isntance without running a dev tool ? the probably can, how can be figured out
- the goal is to either have a new cljs-tool that would provide nrepl (maybe Socker REPL will do? ) or not use nrepl at all (most likely)
- need to look into what Socket repl is and make cljs-everywhere work
- boom, beatch


## or: vscode + background jvm process

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


## repls and tabs

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


## understanding nrepl, clj repl, cljs repl and shadow-cljs: Death Star tabs are like shadow-cljs builds

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


## how to approach creating a multiple-tabs-nrepl environment for the game ?

- jvm is not the issue: it's a different kind of functionality needed for the game, shadow-cljs does not cut it
- first, it should be built game specific, and from that a generic abstraction (tool) may appear
- how it would look like
    - the key is to expose nrepl protocol
    - the rest would be game specfic processes providing compiler states, tab connections and tab evaluations
    - tab app for solution space and resource space would have a process that would have an :eval op (of js code)
    - the other parts of the mechanism would be part of the extension runtime, doing necessary switching and compiling cljs to js, sending to tabs
- game specific, execpt maybe for nrepl-protocol server
- done this way, any extension would be able to connect 



## thinking nodejs

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


## server conundrum: nodejs or jdk, again

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

## beyond jvm just for server: vscode + jvm

- if only host runs jvm, there is no way to leverage tooling such as nrepl
- so the first-step solution (before these abstraction can be run on nodejs for example) is to run both vscode and jvm and distribute operations accordingly
- and it still possible to keep it unnoticable, as jvm can be jpackeged and background downloaded/updated by extension
- memeory is not a concern, at all, only value is
- vscode + jvm on each machine allows both to be host and leverage all the tooling available
- yet, overtime abstraction may migrate to a different runtime


## vscode + jvm design


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


## state: it's not about a repl into tab, it's about runtime-less language, data, state

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
    - before simulation, when code is submitted, the game will read player's file, send it and eval or eval it on worker and send resulting state to the server (yes)
    - code is evaled locally (on the worker) to get the state (data) of the game 
        - to start, fiest scenarios will have mirror maps/worlds where players' score matters
        - so each player has it's own state, and then the score is compared
    - then that state is synced with the server and other's
    - players experimentation state is also synced with others continuosly - so everyone sees what others are up to
- what tab is
    - tab is a glorified renderer over channel
- discarding/creting copies of palyer's namespace in clojure
    - so player get's a generic nrepl into jvm (boom, boom, boom!! oh, pain is gone..)
    - into their namespace
    - if possible, there should be created copies/snapshots of it (not necessery, just an idea)
    - so player could start a-new
    - but: only state is what matters, and it will be isolated and synced 
- evalution
    - evalution is an event, an input, an action (like a click or press of a button)


- possiblity: syncing players' code file
    - so on interval a player's file is read and sent to the others (no need to sync anything, jsut send)
    - and if a player chooses, they can open an editor tab (literaly, a file on their disk), that is constatnly re-written (as it comes over network)
    - it's an option, a thing to consider for observing the game - to allow observers to see player's code (in addition to graphics in the tab)

- how to have independent switchable game states (for each player) in one render-tab ?
    - approaches
        - namespaces
            - each player's state is synced and arrives and is applied to it's namespace in that render-tab
        - or: that state is kept on the worker/extension and player can press an button ( extension op, no tab)  and tab is re-created with that player's state
            - for example, there can be a scenario tab and a lobby tab, sceanrio in top-right quarter, lobby bottom-right quater, code is in the left (whole side)
            - lobby tab is part of the extension, it provies lobby ui and other game operations 
            - and there could be an op "show me this player's state or get back to mine"
        - or: render-tab has extenion logic and operations , scenario is run inside a section/iframe or something
            - so it can be quickly re-drawn (as each player's state arrvies to the render-tab
        - or: render-tab has extension logic, plus scenario is designed to render multiple player's states
            - scenario's rendering is aware that there will be multiple players
    - to keep in mind: scenario should be powerful, free, even if it means more logic

- render-tab: using it for both game ui and scneario rendering
    - hear me out
    - render tab has it's own ui (lobby, actions etc.), namepsaces and tabs (for example, for each player's view)
    - ui is that of the game, of the extension - one ui app
    - the main view shows has iframe(s) that renders sceanrio's graphics for each player (or one for everybody)
    - can iframe communicate with parent documnent ? messaging ? need to be checked
    - the point: it can be single tab approach, which would be better use-wise (code on the left, ui on the right)
        
- render-tab: separete tab for game ui and ability to open multiple tabs for scneario states (for each player)
    - for example, top-right quater has game ui (lobby and other, complete app)
    - inside that app you can perform operation "open player2 (3,4,5..) game state"
    - in the bottom-right quater in addition to your tab opens the second (3,4,5..) tab for other players
    - so the question: what is better - react tabs and iframes or vscode tabs

- render-tab: iframes vs multiple vscode-tabs
    - because extension contorls state (and tabs): it keeps data exchange open at all times
    - but with iframes, game ui will be in charge of iframes, which is wrong: it's just a render-input mechanism, extension has the state
    - sceanrio tabs can communicate with extension (which is what's needed), whereas iframes can only talk to parent doc
    - however, iframes messages can be proxied to go to extension and from there game ui will be updated
    - with iframes there is more control over ui: certain elements can be absolute-positioned on top of iframes so it's non-limiting ui-wise
    - yes, consider proxing from iframes

- render-tab: game ui and scenario share the dom
    - game ui renders scenario into a section
    - to be precise, it's the sceanrio that is in charge (via api/channels)
    - all ops are on extension side, and prefreably runtime-less
    - scenario uses extension api and also some rendering api within the tab
    - but it's the extension that starts the scenario process (but scenario renders itself into a section by id)
    - so extension is a process, render-tab is a process and sceanrio comes in as a process

## simulation as f(state,code,time), why there is no need for cljs self-hosting

- all happens on the worker
- player has a discardable/restorable namespace with its state
- when player evals, they alter that namespace, creating variables and changing it's state (atom)
- when core is submitted
    - players file is read (before the simulation)
    - a fresh copy of the ns is created, players code is evaled, state is reset! to game state at that time
    - player exposes some api - fns or variables, it's a program, a ns, and fns depend on variables
    - then players code os used to run their simulation on their worker and get their new state/score
- simultaion
    - simulation is a process
    - and is part of the scenario
    - it moves with a certain pace (controlled by timeouts,sleeps)
    - it changes solution space's state
    - it uses player's code where it supposed to
    - say, it runs for 15 seconds, advancing state every second (15 steps)
    - every time state changes, it is conveyed to the tab and merged/reset into state for rendering
    - if error happens, it is also sent to the gui to and displayer for the player
- using state for rendering, not evalutaion
    - workers and server are source of truth
    - and game simulation is runtime-less: can be run on jvm, node and in browser
    - this is why player's namespace and variables are needed only on the worker(or server), for rendering the resulting state is enough
    - every time state changes (when player experiments for example) it is sent to the renderer
    - but ns and variables exist only where the simulation needs to run and where code is submitted - on the worker (and server)


## inputs (operations) from scenario's ui (if any) should map to programmatic api unequivocally

- any even from the api should map exactly to an opeation exposed programmatically
- if a player can move something with mouse it should be available from the repl as well: (move {:somehting :x :y}) 


## runtimeless abstractions - user and hub, extension/worker/server - and why worker and server are single abstraction: server

- the initial thinking was: woker is extension's subrpocess, that provides jvm runtime, and server is a separate abstraction for multiplayer
- but, this is incorrect
    - extension handles ceratain ops, but mainly user input and rendering
    - worker is indeed started by extension is child_process
    - but it's the worker that runs evalutaions, simulations and keeps state
    - it's also the worker that reads fs (configs and sceanrio files), loads sceanrios - so worker hosts most system processes
    - but processes are logically runitmeless and abstracion-wise there are only: user and hub
    - user represents extension + gui + worker, where is hub is somehting that handles multiplayer
    - but: every user can host a server, so where is the line between worker (user) and server(hub) ? there is none
    - as with Docker Swarm, nodes are nodes, and can use consensus mechanism to elect source-of-truth nodes; but every node can be promoted/demoted
    - now with Death Star: it's about multiplayer, being able to host, but at the same time play when offline; so what is multiplayer then ?
    - well, multiplayer and gamestate are runtimeless abstractions (cljc namespaces) that expose channels, processes and api; they can run anywhere, it's just channels, processes, data
    - and if you want to be able to play offline, you still need to connect to something and run simulations (play the game as it is)
    - that something can be a separate abstraction, duplicating server-hosted multiplayer: but, with channels this is meaningless, as these abstractions are unaware whether value comes from socket of from local process - it's a value on channel; damn, CPS!
    - so 
        - there is no difference - no difference - between running a hub for multiple players and running it for single palyer - it's still the same logic (simulations, join/leave)
        - 0,1 or more design
        - plus, when offline, you would want to be able to run simulations against AI (bots), which is exactly the same multiplayer
- connecting from extension
    - any worker is hub, it's a standalone node of the system 
    - and workers already have http server running because of socket connection
    - so worker is a host:port, an endpoint
    - and extension can choose which worker to connect to
- workers
    - worker is a server, a standalone node
    - it's a server, and extension is a user(client)
    - with deathstar.ltee it's a server
- so what that means
    - it means, exntension should be able to connect to the server using some idenetity (uuid from settings.edn for example)
    - so you could open several editor winodws, select settings1,2..3.edn (present in ~/.deathstar/configs) and connect to the server using that
    - you can even not start your own server, but to connect only; but to play (even offline) you always need a running server - no problem, extension will start/stop it as child_process


## multiplayer(hub), gamestate and submitting code

- extesion reads files and sends to the server (hub)
- gamestate can either be a channel (an process(s)) of its own, or be hub's dependency
- I think, it is better to run a more generic version of gamestate abstraction over gamestate channel, on par with the hub process
- so hub processes forwards operaitions over to gamestate over channel
- and hub has references(channels) of runnig simultations by uuid - so gamestate is generic system that runs code and sceanrios
- * scenario loader will be standalone as well

## multiplayer simulation

- simultaion is a processes, provided by sceanrio
- with single-plaer simmulation each runs on its own and players score
- with multi-palyer simulation it's different
    - so it takes as args fns/data (api that players provide), for N players, not just one
    - and it runs each players code at each step: (player1-fn ) (player2-fn) .. against their part of state
    - so on each simulation step every palyer's respective code is applied and we get the resulting scenario state (for example, all new positions of all rovers)

## how gamestate will run submitted code (files)

- all files are passed to gamestate, changed if needed and evaled - so that new namespaces for that version are created
- then vars (in those generated namespaces) are passed to multiplayer simmulation as args
- for example, generate new ns name by using base and gensym: (gensym "deathstar.scenario.rovers1.player") -> deathstar.scenario.rovers1.player67
- after simulation completes, sync state with user-side and use ns-unmap or remove-ns

## request, response over socket: http is needed, gui as render-input only

- you cannot do request-repospose over sockets (alas), only inside a runtime
- implementing your own is out of the question - its insane to re=invent the wheel, http already exists
- now, tab gui should not make requests - because extension may need to incercept as well ; and it's unclear if you can from vscode tab
- but you shouldn't: gui should only render from given state and put! input events
- those events go directly into extension over tab channel
- extension gui-ops
    - takes inputs
    - make http requests (from node, so no cors, and you can block-wait for response)
    - and swap! local state (atom)
    - that atom has (add-watch), which on every change puts an :reset-state op to gui
    - gui simply takes data and does reset! on a reagent atom 
- this way input processing is also runtime-less ( will be run on node, but is non-specific)
- so user-side and server-side communcate via http and socket stream - socket stream is used for gamestate updates

## http as a channel for values

- mapping spec-validated runtime-less values like {:op ::spec/foo :data {:a 1}} to rest makes no sense
- and graphql is showing that you want to use http as a channel for queries(requests)
- you want request/response support, but without rest (unless you specifically needed)
- Death Star extension-server communication is a channels for values: some go as stream (over socket), some need req-resp capabilities
- that is why
    - use one endpoint /api and post values as you would put! onto a channel
    - all requests go into  a proc(or maybe parallelsim will be added later) and is handled as usual : as op and value 

#### back to worker: as the first step, every player needs a local worker to have their own nrepl connection

- simple: you cannot intercept editor-nrepl communication, so there is no way to identify the connection on server side
- if all players connect to server's nrepl, they will each get a session, but no way to identify players
- with local node, it's possible

## wrong: right now, the player can be idetified by unique generated namespace via first eval op (in-ns 'deathstar.scenario.rovers1-e123edn)

- in theory, possible to map such op to nrepl's session
- when (in-ns) arrives, that ns is the id of the player
- for example, suffix can be first 5-7 characters from players uuid (or symly gensym that is mapped to uuid)
- it is still better than worker -> server-worker synchronization
- and long-term this is more elegant design-wise
- * possible that mult will provide api (a channel) to attach ceratin data (id) to nrepl ops


## there is no gui: there is extension and renderer

- state exists on extension, and it is specced using extension.spec, like list of settings files, connections etc.
- that state's spec - is needed inside gui to access data keys (they will be fully qualified)
- extension already imports gui's meta to put hypothetical gui ops, but this is aloso wrong
- inputs comming from gui or ediotr commands - are the same inputs from user and are one set of operations
- set of operations
- set of operations, that is extension-only ops
- back to gui needing extension state's spec: gui would need to import extension meta, and that would be mistake of mistakes
- if two deps depend on each other, it's a deign flaw, indicating that they are one thing
- so what is gui? gui is an extensions namepsace (render), that happens to compile into it's own artifact ; but it's only a renderer and inputs (values) stream
- what that means
    - extension has a spec with input|, that is processed alongside cmd| - both are user inputs, representing extension ops
    - gui is a renderer

## player profiles: profile = ns = dir, editor window per profile

- each players config file(s) and game code resides within a directory (ns, profile)
- that directory contains, for example, _deathstar.edn - config of tht profile (same as deps.edn or pom or package.json)
- profile is a git repo: by default, such dir is generated by extension, but user should be able to opne their own, so it can be their git repo
- each editor window can open one profile first (multiple after)
    - first, on extension activation, find deathstar.edn and apply
    - later upgrade: if multiple are opened, the user should be able to select profile from a menu

## starting the server: from extension or on its own? should there be server config in deathstar.edn?

- first step - building the edition with server starting on its own
- focus on the game and events
- as the next step, server will be able to be started from extension (either using cli and child_processes or preferably docker http api)
- so deathstar.edn contains only data about server to connect to, no server launch options

## hub operaions (e.g. list-users) should seemlessly use both http and socket channels (which are just pipes)

- when http-chan is used for request, the response is piped to hub.chan/ops| on the userside
- same should be for socket events (updates): hub will broadcast to all users fresh user-list using {::hub.chan/user-list :response} operation
- it will go over the socket, but value will end up in the same channel as if it was requested via http-chan
- bottom line: http and socket are just piping for abstract operaions, so "how" the value arrives is unimportant, it is all the same for proc-ops

## scenario's gui inputs are code to evaled within the same nrepl session that player uses from the file

- any operation in scenario ui (hover to get info, select ...) is direclty using gam api (same that player uses from repl)
- e.g. hovering is something like (find-entity ..) etc.
- gui pipes those inputs (code to eval) to extension and it should send them over nrepl socket as if they were evaled from file, literally
- options
    - somehow access the nrepl connection/client that clojure extension uses
    - establish a second connection (somehow get from extension the session of the main nrepl connection)
    - ..
- WRONG
    - hover is just hover, no ops needed (data is already on ui)
    - first version of deathstar.ltee will not use any inputs outside of nrepl evals, ui is for rendering/representing data that is part of game state
    - game states are streamed to scenario's gui over socket in full

## hub and users: hub knows only its own operations, if hub needs to send a value to user(s), it is always one of hub's operation

- the question is this: should remote(user) be an abstraction with its own channel api? no
- hub is an abstacration, that has 100% operations
- users are represented as channels, and each user(remote) uses its copy of hub.chan api
- and hub.tap.remote is the additional process that taps into requests/reponses and forms remote's state
- so hub.tap.remote should not - by design - have any api (except for possibly querying state - which is atom)
- if hub would use some remote.chan api, and remote would use hub.chan api - it's bad design, as two abstractions using each other is most likely one abstraction
- so hub has operations
- if so, than every remote(user) is abstracted as simple channel, into which hub puts values created by its own channel api - hub.chan
- for example
    - on user-join hub would broadcast new user-list to every remote
    - it would iterate over map/coll of {user chan} and do (hub.chan/op {:user-list :response} user-list user-channel|)
    - or within a game, hub would similarly broadcast only to users within the game

## user as channel, :user-connect, :user-join and why hub.tap.remote should have a recv| = user| = hub-stream|

- user first connects to hub (as anonymous connection) - this is :user-connected operation  in hub.chan
    - this is exactly what happens when socket connects and send| for that user is provided
- than user sends :user-join event with :uuid and other use-data, hub upgrades that user from anonymous to one with identity
- why hub does that? so that abstraction was independent from socket server specificity, 
- when runtimes are different and connection happens over socket, send| is provided by socket server
- but within the same runtime, where does that send| come from? and if over socket, what represents that send| on the user side?
- since there is no remote asbtraction - it's just channel - it can be provided as simple arg to :user-connected op
- but since it will be used to form state on the remote, it needs to be part of hub.tap.remote (possilby as argument since it has no channel api)
- this way, on the user side in main, it can be rewired like {::hub.tap.remote/recv| ::socket.chan/recv|} - send all values from socket to the tap process to form state
- and within the same runtime, every remote would provide its own :recv channel to hub on :user-connected, leting hub use that as user channel
- what is user channel / recv| / remote-channel? 
    - from hub's perspective, it's a persistent out|
    - so it is not part of hub.chan api, it's an argument ro :user-connected
    - tap.remote has no chan api, so it will an arg to proc-ops

## hub and userside abstractions: extension(runtime) should be abstracted into a channel

- hub can emit operation/request :create-files on game start (and it expects a confirmation/fail from the userside)
- so stream of hub events is not one-sided: confirmations are needed
- as game runs on the hub, every say 30sec hub will request :read-files from user, and expects a response
- so hub and userside are runtime-less abstraction, forming the game system; :read-files should be an operation, and actual fs.read a sideeffect
- mistake is to think about the user-side as extension ops; userside is part of hub abstracation,  including fs operations
- userside proc should run on extension (which is just a minimal runtime with channel piping) 
- and when user-side needs to wait for, for example, actual fs.read, it would use extension(host) represented as channel
    - (<! (host.chan/op {:read-files ..}))
    - (put! out| file-content) -> this goes back to hub
- this way user-side is synchronous when needed, and hub can be aware of user-side ops that succeed/fail
- that is imporant for disconnects 
    - if user disconnects and then connects back, not sending the file content, hub can be aware that some users files are missing
    - it either can repeat the request(or, userside will resend without the 2 request) or it can change the score for that user to 0 for that simulation run
    - point is, hub and userside act as a system

## hub is the system, userside is state sink and input stream like renderer is for userside

- abstract runtime away as a process with a chan api (fs, show-info, render)
- hub perfroms those ops over user channel
- hubs forms user state and sends updates (to merge) into  a simple process on the userside for rendering etc.
- should there be a userside abstraction with chan api ? 
    - probably not
    - should not couple user-state formation process (get value and merge, one op) and remote runtime as api
- use socket only
    - hub can make requests to the user-side, the only way to do so is socket with operation ids
    - and if such meachanism - request/response over socket - is needed for the system, it would be simpler to use it instead of both socket and http

## rethinking is needed: users should be able to intstall game as one thing and run/play scenarios offline

- after installing Starcraft, it's clear how important it is to be able to run/play the game offline, independently
- and game should be *a* thing, intall-launch-try a scenario
- the game needs an editor, so if game is not an extension to an editor, it hsould have its own *and* option to use external
    - both editors use files, so there is no hard dependency on your own editor, it's a choice, but a palyer can play from the get-go, and pros can opt for a diff editor later
- game running locally and user experience
    - it's crucial
    - if a player cannot even isntall/run/test a scenario locally (offline), without a server, it's lessens the experieince many folds
    - so yes, there is docker, and player can launch their, but this needs to be thought over - first, waht should be, then how
- VScode extension and understanding that gui is a single tab/renderer
    - see previous notes, but even within VSCode, game is supposed to use a single tab for gui
    - so the vscode <-> server-in-clj is dictated by "react is needed for rendering, nrepl is needed for the game"
- current next step: requests over socket
    - as of now, to make hub on the server read files, it requires(and that's cool for this architecture) to be able to make requests against user-side (vscode runtime) (see previous above)
    - but: it is a big decision, to build the server this way, instead of buiding a game as a local palyable app and then sycning state
    - game-app-that-is-playbale-offline-and-can-connect-sync-state-with-server  vs userside-is-a-renderer-inputs-streams
    - let's think over to understand
- current design and options
    - server is server, it stays, the question is how much it does and what abstractions/logic run where, what user-side does and what server does
        - is user-side a state sink/input stream, with server doing socket requests
        - or is user-side capable of downloading scenarios, running the game offline and server is for db/state sync
    - we could implement nrepl on node, than would it allow VSCode side to be compelte, apart from server
    - we could make user-side as jpackaged app with its own editor and options to use external editor
    - ...

## importance of react for rendering

- cannot be underestimated: react, antd and other tools are not a minor thing, they cannot be replaced with javafx
- rendering of game app ui and scenarios should be done with react or an equal/better alternative, which there are none

## implementing nrepl on node: this is waht stands in between vscode extension and complete offline-playable game

- if there is a possibility of loauching an nrepl on node, than it would be possilbe to have the complete game in vscode(electron)
- what does server do in that case? sysching state and events db? maybe only sysching state, so events can be hosted against AI
- but that is again the server: just lauched locally! so it's not about server not doing somehting, it's about game app being capable of running the server
- should every palyer have their own repl or connect to server? be it local or remote?

## jpakceged java app with embeded webengine for rendering gui in react

- it's like electron, but on jvm
- then again, nrepl needs work (it's not programmable enough, might require forking, and it's simpler to implement it properly, runtime-less, for node/jvm)
- it does not have an editor: some *have to implement an embedded editor for the game* and have an optional external editor
- external database is still not cross-paltform launchable without docker, so this "one jvm app for local and server" dies if there is no proper jvm-embedded db

## again again again: it is always a server, lcoal or remote, explicit or implicit, always 0, 1 or more

- regardless of runtimes etc., we always connect to the server - local or remote
- we can play offline by lauching (implicitley) a server, connecting to it and running/playing scenarios as 1 player

## one app dilemma: it's either vscode + hosted nrepl + nodejs server, or jpackaged app + make-embedded-editor

- if vscode
    - need to make a hostable nrepl
    - need to run server on nodejs
    - embedded db fo nodejs
- if jvm
    - jpackage
    - make your own editor
    - embedded db for jvm

## jvm app with webengine rendering: electron-like, but on jvm

- that would allow to use npm libs for all gui and making an embeded editor for the game
- it's jvm, but rendering done with browser tabs; possible ? why not electron?
- why not electron?
    - if "one app", than server needs to be embedded (for multithreading)
    - nodejs threads? how important is jvms multitheading?

## what was missing in understanding: it should be jvm, but with js renderer for gui

- it should be jvm desktop app with embedded db
- but instaed of javafx etc., web browser tabs (like in electron) should be used for rendering/input stream
- rendering/input stream as thin layer, all logic on jvm, obviously (same as vscode extension + render tab)
- build-sie it's simple: launch shadowcljs to build page(s), watching will rebuild them, so multiple runtimes are unoticible and it feels like one app 
- questions
    - reasearch what is the way to do it
    - how will tab (js render page) communicate with the app?
    - how to do it so that whole gui (like in electron) is js-based ?

## external editor first for jvm one-app

- first, use external editor (e.g. vsocde)
- once the game works as with extenral editor, it would be possible to add embedded editor
- this would allow for design to stay decoupled and file-driven

## when to launch server: always, server is integral part of user app

- since the objective is one-app, user should always be able to play scenarios offline (locally)
- that means, that server must always be luanched as part of the app and we could select scenarios and play offline
- deisgn-wise it could be abstracted as user always having a list of servers to choose from, the first and deafult always being `local`
- one the game is installed and launched, user can see (in file or in gui) a list fo servers constisting of `local` option only
- then the user can add more endpoints (servers)
- yes, we access the server only via socket/http, but it's always launched, otherwise the one-app is incomplete
- OTOH, if we don't want to occupy those resources (http server, nrepl server, database), we could wait for user to actually want to play offline
    - so we defer local server launch until user opts for playing locally
    - so that if we want to play online-only (on a remote server), less resources are used
- but: we don't care about memeory, only CPU

## distribution: uberjars and beyond

- the goal is proper user experience with installable/ auto-updateable app, like electron provides (and probably Qt etc.)
- to note, as it already works out-of-the-box, uberjars can be launched via double click, so users can download the binary and launch by simply clicking
- it's not a solution, but a clear already existing/working distribution mechanism
    - install java (jvm)
    - double click the downloaded artifact (uberjar)
- but that's not *the* solution, as the app needs to be updated
    - as the basic, GUI can inform user that new version is available
    - we can click and download a new binary and can inform "stop the app, launch the new binary"

## desktop app as a composition of apps(services/processes)

- explore the possibility of app being comprised of apps, some may run constantly in the background
- it would act like docker, exposigin some kind of api, so that leading processes can access the system and start/stop other apps
- but code-wise, apps would be designed unaware of how the run
- obviously, such code can be part of the app itself and processes can be core.asycn processes, so this can be achieved programmatically
- but from distribution stadnpoint and ability to have background processes, there may be existing soltuions/approaches that would be better 

## mistake: premature optimization, first step should clearly be single jvm with server app started/stopped in main/ops

- simple: the first edition of the game (features and everything should be built)
- preserve namespaces, abstractions, decoupling using channels, but within a single jvm
- as it is the optimal way of delivering the working app
- next will be next

## why user app (gui) should be dekstop at all, not a browser app?

- because file system
- users need to be able to edit code files and opt to maintain their game hsitory on a repo
- if game is in the browser
    - we either discard such file hsitory and only keep game hsiitory (so user cannot see the files they've edited the prevoiuos games)
    - or we have to implement our own browser->server pseudo filesystem, which is wrong in every possible sense
- yes, we maintain game hsitory in db anyway, for querying, but it's not files
- if we don't do files at all, and give user text-area per game and then discard, it will evetually lead to "we need a button to save...", so we'll be back on ugly-fs again
- if we want to store game files in a repo and not implement our github, the user app (ui) should have access to file system, which makes it an editor and a desktop app

## we want reagent, we want the ui done with web tools, give user (us) the web page for gui

- it's not just about "can we have react(reagent) for javafx", because nobody knows javafx
- the game wants to embrace the most used programming medium - the browser page - with all it's tooling
- it's not just about a scneario, it's about moving forward with the game having the reach of web ui tooling, that is the leading paradigm by far
- why? probably, because internet and browser->server interaction requiring protocols, and browser api being 3 languages - html, css, js - and not some ugly OOP framework
- but mostly because internet, browser being the most used app after the OS itself, users wanting to freely navigate data in the world, in a lightweight manner
- yes, the border between browser page app and native app is being erased on mobile devices, but still, web ui tools are most advaced and maintained and evolving
- if we go the javafx route only, it means that "make your own scenario" is not done with web tools and is niche
- funcdamentally, ideally, a sceario gui is exactly a program to be run in a browser page enviroment, using react and familiar web


## jvm desktop app vs electron + docker/jvm dilemma, if we require user to have jvm, why not docker? 

- step by step, logical:
- we want and need desktop app, because file system; so editor
- we want react/reagent and web page for user app gui and scenarios guis
- we may have to - be it jvm or electron - launch subprocesses for server and db
    - if jvm, we can embed everything into single jvm, but what do we gain? minefield of corner-cutting
- there is no sane way to have chromium's engine at our disposal on jvm in autumn 2020 and foreseeable future, so can we even rely on javafx Webengine being a legitimate web page environment?
- if we opt for electron, we have to - as in no other way - use subprocesses, be it with child_process or else
    - if we distribute server as uberjar with electron app/vscode extension, we require users (ourselves) to have jvm intalled
        - "hey, intstall the game app and be sure you have java"
        - when user starts the app, it will (when local selver will be needed by player) spawn a child_process with jvm from that uberjar
        - this a dangerous child_process cross-system territory: if user app crashes and comes back, how do we find that proc we spawn? did we even?
    - if we jave to require user to have jvm - one prerequisite besides installing app or extension - why should not we require docker?
        - child_process is a way,yes, but with docker api it is much more sane to find a container by name for example
        - if we exit unexpectedly and come back, we can ask docker (via http api) "hey, is deathstar.server container running?" and go from there
        - sure, we can healthcheck the server itself, even with child_process, but say it reponds inadequtely, how do we find the proc and kill/start a new server? 
        - with docker, we have api for that
- and database: yes, we can look for some db embedable into jvm, sure, but is it a sane approach? relying on the fact, that fingers-crossed, we won't need another subprocess
- and we again, must rely on child_process to manage the database, and hope we don't need to ask user to install somehting else besides jvm
- but if we do ask user to install docker only and game and/or editor, people may just be turned away as it may be considered a bad design
- the positive thing about from the get-go desingning around child_process or docker, is that we can defer lauching anything besides the user app/editor: maybe we only want to connect to remote server, no need for local
- with single jvm, yes, it's the same actually, but we have to invent our apis and hope for db to be embedabble and deal with abandoned gui tools and unclear-if-will-work Webengine
- bottom line question: if we require user to have jvm, why not docker? 


## explicit server data: stored in repo in .server dir and excluded in .gitignore

- when the game is launched, we open - like with editor - a directory, a player's repo, and that's an "instance"
- server data may be stored in a `.server` dir or similar and be excluded in gitignore
- so the system does not create implicit directories somewhere for data, rather the player's namespace is the root

## don't start the server from gui from the beginning, but incrementally

- start it explicitly in docker, same as in dev, in GUI select servers from dropdown
- the way of jvm app means essentially rebuilding an editor, but the goal is to make the game
- editor exists - VSCode - but the missing piece is that server needs to be on jvm, so how do we start it from GUI? we don't
- user installs VSCode and deathstar extension and the can select a server to connect to: local or remote
- local? local server needs to be started in docker manually, it's up to the user
- but this is an ugly user experience..
- but evloution: when the game works with manual server in docker, the "start server" can find its way into GUI
    - first, a programmatic launch via docker http api can be added, so to start user will have to have docker, but extension will do the rest
    - then, docker requirement can be dropped if server would be distributed as jvm and launched as child_process, requiring user to now only have jvm
    - next, server can evolve into a native binary, that will be downloaded the first time user needs a local server, so user machine will have no requirements
- during this time, the user app - editor extension - stays consistent
- but nah, scenarios
    - every user will want the server, the very first time they install editor+extension
    - because, we would naturally want to check out scenarios and play/repl into them
    - game will say "select a server", we would select local, game will say "local server is not running"
    - users will look up docs and see: to start a local server, install docker and do "docker run github.com.DeathStarGame/deathstar.ltee:0.1"
    - natural question at this moment: well, should not that be a press of a button? both docker installation and starting the server?
    - so reaction will be: this is a bad design, misinformation - "can't even play a scneario without some heavy manual setup" 

## game is an editor extension, always, either we build one or use existing

- the fundamental design characteristic of Death Star is that we open a directory on disk with files, create code files for games
- it's a repo, a folder on operatin system, just like any other src ocde repo, but this one has game files
- playing Death Star is editing files on disk in that repo, so we always open a dir and see file tree - we are in an editor
- and editor comes first, game gui and features are an extension
- yes, we can say game comes first and editor is an extension, but this would be a design mistake, as there is language and files first and we play a compepteive game around that
- Death Star game idea is an *extension* of programming, turning editing code into a money-free e-sports scene with unlimited scenarios for an open source game system
- it's an editor first, always


## focus on the what, being able to play scenarios and events, for pros, embrace great tools, compose system of elements (vscode extension + server in docker), go from many to one

- docker and vscode extension allow and inform a decoupled design, thinking
- the single jvm app may not be incorrect after all, but: the mistake is to start with it
- the game - Death Star - as a system, will benefit from healthy formation, like a galaxy and planets from space stuff
- no need to rebuild working tooling, game features should be built with best existing tools
- first: we start with vscode extension and server in docker
    - we focus on the what : being able to play scenarios and events
    - the server may migrate from docker to uberjar to binary
    - but not to start with: we want to be able to play the game by laucnhing vscode + extension and sever manually, and add automation as the next step
- next possibility
    - down the road user can be provided with a small installer binary/script: it would literally install vsocde, extension, docker and dockerload the server 
    - the system will still be comprised of elements, and a decoupled explicit installer would do the setup on every system
- next possibility
    - at this point, the game is playable and scenarios are creatable
    - it may no be the smoothest user experience, but we foucs on events and features, scenarios, making the game for pros, not idiotic simpilicty
    - however, at this point the components are known and a new design - for a single jvm or aother - can be developed
- don't build well-made tools, build the game - scenarios and events 

## vscode extension + server is perfect

- we don't want to build an editor, we want the game, scenarios and tournaments
- server is always a standalone app (subprocess or not)
- even in dev it's desirable to be able to separately restart user app vs server
- if server is standalone, the runtime of user app is unimporatant, except for we want web renderer (react/reagent)
- so electron/nodejs is perfectly fine and fit for desktop gui user app
- we defineately want and need and editor, because we want to focus on scnearios and tournaments, not re-building an editor
- so we have a luxury of the best editor out there that happens to have all the desired qualities - web gui with react, clojure-compatible runtime of nodejs/browser
- and further down the road, we are free to automate downloading/laucnhing server and isnalling what's needed right in the game's gui

## leverage deps tool to install scenarios

- for downloading/storing scenarios see how to leverage deps

## since extension (user app) is a remote renderer/input collection/state sink, and server is the system, identity is needed

- just uisng a uuid and ephemeral connection may end up swaying system development into patches and tricks that would not be simpler/faster
- say, we launch the server, no we want to download scenarios or remove a player, how do we know who is the host/responsible?
- of course, we can do some token shown to he host on server launch, but again, that takes as much mind effort as going fot the correct solution
- at the same time, diving into identity without being able to play scenarios.. no good either
- however, these are two distinct paths
    - maunally inveting every such small workarounds to make it work "faster" (doubtful)
    - building the server as the system with proper identity layer (obviusoly, use an existing solution as ory) and database choice
        - if done properly - by choosing existing tools and assembling a system - it can actually speed up the system and simplify GUI
        - why? because if we have proper users and can login/out, we don't need to mock data layer or mock extension, we go for it properly
        - danger is: focusing too much on non-game specific stuff, instead of playing scenarios
        - for example, until we've dropped all this identity/db stuff and jumped into making the game, we couldn't truely progress
        - the real progress happened when we thought of the game as a simple no-db, no-identity system so that 2 palyer can just connect and play
        - and this is invaluable: this appraoch of let's make the actual game, for two player to play, it gave the most results, because of that we finally understood the game design
- we want to build the playable game, but will bag of workarounds instead of identity/database actually speed up? they definately free thinking up and allow to design the system, that's for sure
- can we do both: think palyer-to-player, simply, but build properly? kind of appraoch building thouroughly, but step out (by imaging the system without generic layers) and design free-mindedly? 

## bold: store user files on the server? consider providing browser GUI?

- let's be bold and even consider this
- we can store all game files, from all users on the server, with proper identity layer
- this way, there is no need for server->user requests! NO NEED FOR SERVER->USER REQUESTS !!!
- user will have all the GUI provided to them in the browser
- and server will be the only part of the system reading from file system, and all the data - files and db - will be part of the system (server)
- user-side actually stays an edge - renderer, collects inputs, receives state and all done via proper identity
- user experienece: is the best, as participants don't need to do anything but login
- what are the tooling implications though?
    - simple: system can me made work (with files and everything - EVERYTHING), just without the REPL
    - and this is toatally fine and superb, as it can be cleanly added/developed later

## yes, server and browser tab, best design and user experience

- see above and project talk # 4 on project's youtube channel