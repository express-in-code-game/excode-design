# lab.weekend-2020-07-23

- invited a friend for a visit next week, to critique and debate the idea of such a game
- naturally I feel dared!
- so self-generated childish challenge accepted: make a version of a game to play when friend arrives, so he could realize the fututre of this kind of games

## rationale

- none

## design

- what should happen when friend arrives
    - friend brings his laptop, we install him an editor
    - we clone the repo and start the game from code on his laptop and mine
    - then connect and play a scenario, after which he concedes and proclaims this kind of games 'great' :)
    - a scenario must be fun, yet dead simple, such that a person with no knowledge of the language could learn in 5 min how to play this scenario
- what to avoid
    - solving f-ing fundamental problems
    - "to build this on a weekend, stupid-simple it kept must be" Yoda, 2020
- any rational reasons why this will work ?
    - I've got clojure, clojurescript and core.async, so I can build anything


## notes

### scenario

- start with a more simple/basic scenario
- simulteneous turns (say, 1min), for ease of learning players must press 'ready' before runs
- simple, elegant graphical part (basic geomentric shapes/symbols, words and other values)
- to start, make a  scenario that does not require the player to know the language, rather general logic
    - map is a grid devided into squares (tiles)
    - both players get a droid/rover (a circle/square/some-other-symbol on the grid/map)
    - droid has APIs (listed in resource space): scan move attack1 attack2 attackN research pick examine repair cloak ...
    - each API costs energy
    - each turn player's droid start with 100%, they spend it on move and other stuff, but can find recharges on map (maybe not)
    - each API fn has associated animation with it
    - the player whos droid first gets to the flag (for example, in the middle of the map), wins
    - there are obstacles: traps stones sands fields and other stuff
    - players write fn (or fns) to define droids behavior that is used, say, for three turns: 1min think -> 3turns run -> 1min change logic -> 3 turns run ...

### exts

- base
- connect
- console/tepm/system
- events
- games
- game
- player/fs-palyer
- scenarios
- record
- server
- settings

### walkthrough 4: define system as channels, values, channels api/protocols

- channels and values, channels and values everywhere
- channels should be on the system level, 'static'
- channels should have an interface/api; processes as well, but only for performance purposes and used with singleton processes (e.g. host/editor)
- otherwise it's oop mess with actor model: instances talking to instances
- mult pub mix, tap sub admix - to get transparent and fluid system
- server will literally ignoarantly broadcast events, apps will send or recieve-apply events, because channels are defined
- values and channels: how to compile-time validate that only protocoled values are being put on a channel and value map is correct ?
    - 1. use Vals protocol, create channels interface instances 
    - 2. a vals ns with unique fns to create ops/vals, multiple api ns for each channel which import vals ns and re-expose vls/ops valid for the channel
    - 3. clojure.spec
        - project ops, values, channels types, required map keys for each value map - are defined as data (sets) to be used with clojure.spec
        - note: clj and cljs have speccing mechanism for functions (probably, via macroexpansion?)
        - create a fn/macro that takes :channel-key {:op :op-type ..rest-of-value-map} and on compilation will cause error if that op is not specced for that channel

### walkthrough 3: processes and queues

- "functions make poor machines" Rich Hickey ~2013
- why editors, apps, systems fail ? because it is inherently complex to build using functions when it's not a library
- mechanisms (machines/systems) are logically processes, that should put/take thoughtlessly values on/from queues
- from the get-go even a small system should be built from processes (even hello-world)
- that will create breathing room for adding/removing logic to the system without complexing it
- an extension is a process, so if you have processes, you are one step away from extensions
- what is left to consider is
    - how will values be put on channels (how to do it via a protocol)
    - how processes look up state (menatlly understand it as a db request: one db, many apps)
    - how to make extensions unware of each other and yet convey/lookup what is needed; and what is needed ?
    - how to make it totally about channels, yet via an api
    - if api, how to avoid requiring extensions from other extensions: to keep dependencies wisely distributed per process
    - how to avoid using channels just to lookup some state like :is-running? or :connected? etc.
- render process and its dependencies should be an extension (so deps are not part of the app), so the system/game/editor is decoupled from gui library/other tools
- extension is unique: one-of for the system
- ways to approach channels, processes and api
    - channles have protocols, reify to create interface instance to put/take on channel
    - client: for every(needed) process write a separate client that hnadles discovery/connection, exposes clean api and needs to be created(def) only once
    - runtime discovery: like in vscode, extension exposes a map/api; during runtime other extensions must discover it themselves, but not client needed; again, no compile time errors
    - chaos: no protocols, put maps on channels; no compile-time errors, find ut what's broken in runtime
    - ???
- ways to approach channels, processes and api part 2
    - it seems the only way to have apis and sanity is to define a process (extension) as two packages: meta and proc
    - meta should be dependecy-less, other extensions will import it and use meta.api to communicate with extension
    - meta.api contains extension metadata, that has an id: can be url or hash or any string; if ext with id already exists, the new will not be loaded
    - with channels two options
        - create a client, that creates copies of ext channels and subs/taps/unsubs/etc whenever ext is registered/unregistered
        - since they should be static and unchaging by desgin, define them as part of meta package
        - say, extA requires extB/meta and taps and subs; but extB might not have been installed!; if channels are part of meta, it would not be a problem - values won't be taken untill ext is installed 
        - this way channels are decoupled from the process: extB can be installed/uninstalled without breaking other exts which through extB/meta.api unknowingly depend on the channels
    - when extB is installed, it probably has to get some unique, gensymed namespace; so how do you tell it, that it's channels already exist ?
    - and how do you make ext/meta unique if it is statically required ??? damn
    - scratch that: let the author of the ext guarantee the uniqueness of the namespace; namespaces for the win!
- installing from git
    - app will be a binary, so even if it's possible (using tool.deps) to get ext and it's deps, running would mean relying on .clojure/.m2 dir for jars
- extB depends on extA
    - extB must not contain extA as a dep, extA must be installed
    - if you :require extA in code, those namespaces must be already present
    - it means, that to develop extB, it must be run within a runtime of a running app/editor, that has extA installed
    - how do you develop extB? how do you run it via your own devtools but within app/editor runtime ???
    - if you install extB without extA, app should somehow know to install extA or inform that extA must be installed first; how (preferably without manifests) ?
        - incode manifest may be ok, say, extB.api ext-info to tell that it requires git-url:hash or version (in deps.edn) or smth
        - or throw error; let ext readme tell user which exts to install prior
    - anyway, is it correct to assume that meta is unnecessary ? that anytime you run extB, extA namespaces must already be there somehow ? 
    - yes! it is correct indeed (was a typical cart-before-the-horse, solving non-exiting problem)
        - extension is essentially a namespace, by design; it must be unique; it may have deps
        - when extB deps extA
            - case1, native (included) to the app exts: no problem here - obviuosly all namespaces are present
            - case2, developing extB: creator must ensure that extA is installed (app catches an error on installation of extB and maybe hints 'chck if all the ext are present')
            - case3, user installs extB: same, user sees the error and should check extB's readme - it should tell which exts extB depends on
            - maybe later (not critical,not essential, myabe not) some chain-install mechanism can be added
        - so whenever extB :requires namespaces from extB, they must be already in the classpath or runtime
        - case closed, meta not needed


### walkthrough 2

- simpler
    - choose a gui environment
        - cljfx seems the right tool for the weekend challenge (because clojure only, should be faster and simpler )
        - plus, would be fun to explore javafx tech branch, see how ot compares to browser/electron
    - create scenario generation and graphics (schema)
    - make it runnbale/playable on one computer
    - then, look into networking
- one jvm instance
    - server will be a separate app
    - but for weekend build, consider running it within gui app's jvm
    - so players can 'host' right from the client
- settings
    - add ability to edit edn with settings from within the client (colors are strings "#fff" for example)
    - even give users ability to customize certain functions (like you would in editor) using clj code (so clj editor if exists)
    - key is being able to configure scnenario, preferably without ui (instead, edn with comments for each setting/key)
- ui extensions 
    - base (layout and base gui components)
    - connect (specify address(s) to connect to server)
    - server (configure .edn map that will be passed to server app, start embedded server)
    - games (list of games, create/join a game, game opens in a new tab)
    - game (pick a scenario, configure game, start the game; pick a tournament format)
- how extensions will handle their ui with cljfx
    - with DOM and react, base ui can render a slot div element with id, and extension can use such element as target to render it's subui
    - it is yet unclear how to do it with javafx/cljfx (maybe fx/ext-let-refs)
    - since cljfx provides docs and examples of using standard single tree for the app that uses global state, let's embrace
    - so: extensions will enter and change the state, registering fns/refs to their ui components
    - main ui will use this refs to render subui in app tree (or null if none were registered yet)
    - so content (of a tab or pane or panel) is a value in the state, that is passed as prop to app's ui
- scenario and the system (game)
    - join/leave, connect/disconn to the game are part of the game (system)
    - scenario does its own ui, config, veto, start/stop, load code on interval (using system's api)

### walkthrough 1

- make cljs browser app (from previous labs, comprised of extensions, that uses antd) work in electron
- so it was possible to navigate and add extensions(= views), so now it's like browser app but in electron
- now read/write files to a dir (from settings.edn file, all constants are in settings)
- so electron opens, reads a dir, gets settings, reads other stuff and so the client has started
- networking
    - to avoid nodejs and npm, clojure server must be used
    - so client will connect to such server, which will transmit data
    - obviously, server is started in docker container on one of the laptops
    - sounds like beyond weekend ? but peer-to-peer won't be easier or am I wrong?
    - nah, that's correct
        - client starts, can load scenarios, kind of works, but without connectivity/tournaments, which require server connection
        - now, server should do the least in terms of scenarios: only transmit events
        - brackets and wiki are fundamental, so not now, do only PvP games or maybe a simple bracket tournament
- identity
    - don't get me started; right now implement something in stupidest possible way
- so what are the parts
    - client (electron app)
    - server (one jvm app, no nothing, no reverse-proxy, security etc.)
    - editors are editors, already done
- so what happens
    - client starts, reads files, gets going
    - player inputs server address, client connects via socket
    - player creates a game via http request ? db ? lol, no; lol, yes, as funny as it sounds; peer-to-peer same amount of mess anyway
    - another player connects to server, sees the game, joins
    - palyer starts, files are read periodically accroding to scenario
    - electron app will expose api to scenario; scenario will be just a namespace (not external, git loadable yet, part of the weekend implementation)
    - so that scenario namespace will use fns like 'read-palyer-files' and stuff, 'send-some-data' or whatever; point is: as api
    - so when simulation starts, game gets what player has written, and uses that code in addition to scenario's to run somehting on screen
    - anyway, players score points or whatever, so evetually winner is decided
    - game over
- build
    - on friend's laptop tooling is needed to build from code
    - so: either build a binary beforehand and share that, or install docker, use a container to compile to js, and install nodejs to run it
    - or maybe: release binaries as part of this repo
- how about
    - screw db: no need for the weekend two much mess with schemas and stuff
    - check out java-based desktop alternative to electron ? that would be nice, althow won't have the reach of web tools (would mean no html5,react,antd)
- ~~connecting from editor to get intellisense when editing the file with the game code~~
    - game ui exposes a host:port for nrepl server running locally, player connects editor
    - game dir contains subdirs with generated names, each contains files; both client and editor read those files
    - and it is the client app, that starts a build with an nrepl (so project.clj for example is part of the client, with repl namepsace being subtitued for each new game)
    - so the client app should be capable of starting/stopping such a tool; obviously, it requires jvm runtime; obviously, wuold be great for the client gui to be jvm for that matter
    - in node, you'd have to spaw a child_process (which will have to be jvm with both leiningen and shadow-cljs); damn, rip rich npm-tool-world ?
    - in principle, it should be possible to connect to multiple repls: resource space, solution space and be able to create/discard nrepl sessions
    - client should abstract all that away, proving tips, addresses, start/stop options to the player/observer (yes, observers are basically players in regards to repls)
        - game starts, stuff is generated and (why not), files are created with apis and such
        - player sees addresses to connect repl to, and can discard a session and start anew (via button in ui)
        - cleint should start/stop nrepl server, add/remove sessions, read player files
        - resource and solution space data and api(s) are generated under a unique namespace (same as the ns in game-dir where player edits code)
        - so it comes down to files: scenario generation -> files created ->  nrepl started -> sessions created -> addresses exposed to the user
    - essentially, client reads/writes files, start/stops nrepls, while user's editor only needs to open files and connect to addresses shown in gui
    - so game generates data (code), it is written to files, then those files are evaluated and ,say, '(main)' is run and graphically we see the soltuion space
    - same with resource space
    - then user code file is read, user's api is applied/used in soltuion space code, graphics render the user-code-included solution space
    - wait, why sessions? what about one session (at first), different namesapces (soltuion,resource, user code)
    - this way, lein can be started from shell after files are generated, and client only reads/writes files, no nrepl control
- *heck, for the weekend: no intellisense!!! just write/read files (as text), keep client simple, focus on the scenario fun-ness and graphics*
    - so players can see the resource and solution spaces in gui (hover and click), can edit code with no repl connection yet
    - scenario GUI may be simple DOM elements even, with emphasis on values (numbers, names); so like a schema, and then , say, rover (a dot/square/img) moves on the grid with simpliest animation
- order
    - start with the scenario and game! 
    - otherwise networking will never end
    - once it happens graphics wise and game wise, proceed to networking 



## links

- electron alternatives
    - https://github.com/sudhakar3697/electron-alternatives
- javafx
    - https://github.com/openjdk/jfx
    - https://openjfx.io/openjfx-docs/ getting started
    - https://docs.oracle.com/javase/8/javafx/get-started-tutorial/get_start_apps.htm guides
    - https://openjfx.io/javadoc/14/ javadoc
    - https://openjdk.java.net/projects/openjfx/
    - https://wiki.openjdk.java.net/display/OpenJFX/Main
    - https://vlaaad.github.io/year-of-clojure-on-the-desktop
    - http://tutorials.jenkov.com/javafx/webview.html
    - javadoc
        - https://openjfx.io/javadoc/14/
        - https://openjfx.io/javadoc/14/javafx.controls/javafx/scene/control/package-summary.html
        - https://openjfx.io/javadoc/14/javafx.graphics/module-summary.html
        - https://openjfx.io/javadoc/14/javafx.web/javafx/scene/web/package-summary.html
    - icons
        - https://github.com/kordamp/ikonli
            - https://dlsc.com/2020/03/11/javafx-tip-32-need-icons-use-ikonli/
- cljfx
    - https://github.com/cljfx/cljfx
    - talk 
        - https://www.youtube.com/watch?v=xcMNTKFmEgI
    - https://github.com/cljfx/css
    - https://github.com/cljfx/cljfx#more-examples
        - https://github.com/cljfx/cljfx/blob/master/examples/e07_extra_props.clj
            - nice overall example (tabs with a tiles, imgs, grids )
        - https://github.com/cljfx/cljfx/blob/master/examples/e12_interactive_development.clj
            - chart with animation, example has step-by-step walkthrough
        - https://github.com/cljfx/cljfx/blob/master/examples/e14_charts.clj
            - tabs with charts
        - https://github.com/cljfx/cljfx/blob/master/examples/e16_cell_factories.clj
            - tabs, data tree and excel like components
        - https://github.com/cljfx/cljfx/blob/master/examples/e18_pure_event_handling.clj
            - http request to github to get repo metadata, data tree component to view
        - https://github.com/cljfx/cljfx/blob/master/examples/e19_instant_repl.clj
            - put code into left window, instant eval result in the right window
        - https://github.com/cljfx/cljfx/blob/master/examples/e20_markdown_editor.clj
            - editor in the left window, preview in the right
            - example might be useful in terms of configuring initial size of the app window
        - https://github.com/cljfx/cljfx/blob/master/examples/e21_extension_lifecycles.clj
            - not sure yet what it is
        - https://github.com/cljfx/cljfx/blob/master/examples/e27_selection_models.clj
            - lists and selections
        - https://github.com/cljfx/cljfx/blob/master/examples/e28_canvas.clj
            - progress bar on canvas, code looks concise
        - https://github.com/cljfx/cljfx/blob/master/examples/e30_devtools_via_event_filters.clj
            - compnents change color on hover and show info
        - https://github.com/cljfx/cljfx/blob/master/examples/e31_indefinite_transitions.clj
            - rotations and other animations
    - example of HN app, with cljfx cljfx/css jpackage
        - https://github.com/cljfx/hn
    - cljfx-fy javafx components
        - https://github.com/cljfx/cljfx/issues/41
    - reveal tool
        - https://github.com/vlaaad/reveal
- javafx libraries
    - https://github.com/topics/javafx
    - https://github.com/mhrimaz/AwesomeJavaFX#libraries-tools-and-projects
    - https://www.jrebel.com/blog/best-javafx-libraries
    - https://github.com/controlsfx/controlsfx
        - https://github.com/controlsfx/controlsfx/wiki/ControlsFX-Features
    - https://github.com/jfoenixadmin/JFoenix
        - http://www.jfoenix.com/documentation.html
    - https://github.com/AlmasB/FXGL
        - JavaFX Game Development Framework
    - https://github.com/FXMisc/RichTextFX
    - https://github.com/manuel-mauky/Grid
        - A Component for grid based games like sudoku or chess.
- eclipse
    - https://www.eclipse.org/articles/Whitepaper-Platform-3.1/eclipse-platform-whitepaper.html
    - https://www.eclipse.org/articles/Article-SWT-graphics/SWT_graphics.html
    - https://www.eclipse.org/swt/widgets/
    - https://en.wikipedia.org/wiki/Standard_Widget_Toolkit
- clojure and GUI
    - https://clojureverse.org/t/building-a-non-web-gui-app-with-clojure/5026
    - https://github.com/fn-fx/fn-fx#a-note-on-javafx-vs-openjfx
    - https://www.reddit.com/r/Clojure/comments/dbqq0h/question_fastest_way_to_get_started_in_desktop/
    
- javafx scales tiny on HiDPI ubuntu
    - https://github.com/defold/editor2-issues/issues/563
        - -Dglass.gtk.uiScale=2 works

- merging lein profiles :jvm-opts
    - https://stackoverflow.com/questions/38054983/use-leiningen-aliases-to-specify-jvm-flags
        - https://stackoverflow.com/a/38097107/10589291

- macro expansion
    - https://clojure.org/guides/spec#_macros
    - https://github.com/clojure/core.async/blob/e2fcb458b58f8aed2dd0390903338c9331bf0620/src/main/clojure/cljs/core/async.clj#L103
    - http://bryangilbert.com/post/code/clojure/anatomy-of-a-clojure-macro/
    - https://ask.clojure.org/index.php/9015/spec-error-on-simple-threading-in-clj-cljs
    - https://stackoverflow.com/questions/37048167/what-is-the-difference-between-macroexpand-and-macroexpand-1-in-clojure
        - https://stackoverflow.com/a/37048974/10589291
    - clojure 1.9 alpha ANN in clojure mlist (specs for macros)
        - https://groups.google.com/g/clojure/c/_slHTn-Ej1Y/m/M_IVRODtCQAJ
    - assert-args macro that leaves no code (code exists and runs during macroexpansion only)
        - https://github.com/clojure/clojure/blob/clojure-1.10.1/src/clj/clojure/core.clj#L1832