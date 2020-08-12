
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