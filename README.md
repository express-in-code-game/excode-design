# lab.weekend-2020-07-23

- invited a friend for a visit next week, to critique and debate the idea of such a game
- naturally I feel dared!
- so self-generated childish challenge accepted: make a version of a game to play when friend arrives, so he could realize the fututre of this kind of games

## rationale

- none :)

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

### walkthrough 2

- simpler
    - choose a gui environment
    - create scenario generation and graphics (schema)
    - make it runnbale/playable on one computer
    - then, look into networking

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
        - "compile-and-restart-the-app-and-then-navigate-the-UI-to-see-your-change" - nah, you can simply eval '(render)' in browser/node, opt out of reload-on-save
        - https://github.com/cljfx/cljfx
            - not CSP, alas
    - http://tutorials.jenkov.com/javafx/webview.html
- eclipse
    - https://www.eclipse.org/articles/Whitepaper-Platform-3.1/eclipse-platform-whitepaper.html
    - https://www.eclipse.org/articles/Article-SWT-graphics/SWT_graphics.html
    - https://www.eclipse.org/swt/widgets/
    - https://en.wikipedia.org/wiki/Standard_Widget_Toolkit
- clojure and GUI
    - https://clojureverse.org/t/building-a-non-web-gui-app-with-clojure/5026
    - https://github.com/fn-fx/fn-fx#a-note-on-javafx-vs-openjfx
    - https://www.reddit.com/r/Clojure/comments/dbqq0h/question_fastest_way_to_get_started_in_desktop/
    
