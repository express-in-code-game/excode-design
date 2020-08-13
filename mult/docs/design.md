# mult: clojure(script) extension for vscode

## rationale

- clojure(script) IDE experience is no minor issue - it's the thing between you and programs
- the editor and the extension 
  - should be long-term satisfactory, enjoyable and even inspiring
  - should be open source
  - should be written in clojure, or at least the extension should be written in clojure(script)
    - for simplicity
    - for asynchrony done via processes (core.async)
- the extension should
  - support multiple repl connections from one editor window
  - have a file configuration (for user and projects), where connections and repls can be specified (to not depend on key-combo connection sequences)
  - be simpler, code-wise and feature-wise 
- making an editor in clojure is, no doubt, a goal, but the extension for an existing editor is a logical first step
  - the work of making an extension is trasferrable even into an editor written in clojure, so the work won't be lost
- existing editor + extension combos
  - Emacs + Cider
    - perfect, if you're into it
  - IntelliJ + Cursive
    - ~~both are closed source~~, Cursive comes with conditions
  - VSCode + Calva
    - can be considered the current best option
    - VSCode can be considered the best open source editor
    - Calva works perfectly, but is written in typescript
    - nodejs runtime is undesirable, but not a problem
- what exactly mult's design and value is ? what's the trigger to bother at all?
  - sometimes (rarely, but it happens), you run multiple apps that form a system
  - it's not an every-day thing, but when it happens, pain follows
  - so what you want is this
    - open one editor window
      - which (via workspaces for exmaple) already support multiple directories(repos) per workspace
    - add to that workspace all those apps(repos), start the system, so now multiple apps expose multiple nREPL connections
      - some nREPL connections - like shadow-cljs provides - expose 2 logcal repls (clj and cljs) by deafult, so it's one-connection:multiple-repls already by nREPL design
    - navigate between src code of those app and your REPL tab should follow you, switching connections and logical repls as you specified them in one config file
      - example config [mult.edn](../examples/fruits/.vscode/mult.edn)
    - that config says: hey, here are connections (separately), here are repls (that use those connections by key, but are separate) and here are tabs (every tab has some repls)
    - plus, that config has eval-able functions that help you easily (no need for sub-language) using clojure write a regexp for how to tell which repl to use for which namespaces
    - what you get, is an extension, where connections, repls, tabs are separated and a new repl, tab can be added/removed as needed
    - additionally, extension should handle reconnections by default, when you start/stop apps - if repl is within config, extension always attempts to connect; no key-combos on every restart
  - is such extension features somehting that is NEEDed ? Kind of yes, but not every day, that's true
  - but
    - there is **no reason** why extensions should not by design be 'zero, one or more'!
    - why is there a limitation of one window, one app (process) ? or hard coded workarounds? 
    - it should be by design: 0,1 or more connections, repls, tabs, configurable via file
- mult itself (as a VSCode extension) is an example of a project, that needs mult
  - mult is developed using shadow-cljs, which thankfully supports mutiple build (apps)
  - and mult consists of 2 apps
    - [mult/src/mult/extension.cljs](../mult/src/mult/extension.cljs) - extension itself
    - [mult/src/mult/impl/tabapp.cljs](../mult/src/mult/impl/tabapp.cljs) - react app that runs in the tab (VSCode is built with electron and tabs are actual browser tabs, isolated runtimes)
  - existing extensions support this already - you can manually select(switch) shadow-cljs build (from :extesnion to :tabapp for example)
  - but what you would prefer is to define in config file, that tabapp.cljs corresponds to :tabapp build and extension should do the switch automatically (over nrepl, as an :op) when the active file changes 
  - again, an eval-able predicate in mult.edn is used to determine which file corresponds to which repl(s)
  - for shared files (used in both apps) the preference can be set in the config file, still allowing to manually select(pin) :tabapp or :extension (this is how existing extensions approach it)


## should be possible

- have multiple repl connections from an editor window
- every repl tab has a list of connections (1 or more)
- every tab has a (savable in settings) switch which runtime to prefer for cljc files - clj or cljs
- when switching between files, tab switches namesapces if they are relevant to connections
- these settings (sets of connections and preferred runtimes for cljc) are savable in workspace's .vsocde/settings
- point is: one editor window should allow for 
  - seemless navigation between clj cljs cljc files 
  - evaluation of forms in their respective runtimes and connections
- configuration file(s) should be part of the repo
  - saved, e.g. in .vscode/mult.edn
- when opening repo in vscode, mult reads config(s) and sets up all connections and repl tabs 
  - * if user chooses 'set everything up as in config'
- mult exposes a menu with all this options (in a tab, or dropdown after press)
  - you shouldn't have to know secret keybindings to get a list of available options(actions)
- evaluation2
  - should be always possible (besides automatic/config):
    - open mult tab, add connections (connections are uris)
    - mult will switch connections when files change
    - but: tab may have a list of connections (like tags) on top, user can always override and select a connection
    - if so, anything from anywhere when evaluated in this tab, is sent to that connection
    - or user can by clicking switch tab to the another connection
    - or user can switch to default behavior
    - point is: user has full control over connections
      - they don't result from keycombo-popups
      - autodetection (if any) is secondary to: config, specifying connections as strings
      - connections are just connections and can be added/removed to/from repl tab(s)

## notes

- mult.edn 
  - is either in .vscode/mult.edn or in ~/mult.edn
  - has :connections :repls :tabs
- connection
  - physical socket (as of now nrepl) connections
  - knows how to connect, send,receive
  - from system perspective, a channel
- repl
  - logical repls: one actual connection may have multiple repls (e.g. shadow-cljs)
  - knows how to switch the connection to itself
  - knows nrepl or other operations: eval etc
- connection and repl
  - connections are established via a click or if :connect/auto? true
  - when first eval happens, repl tries using the specified connection : conn channel is the arg to repl
  - no init-ns in config: once mult opens, it works as should: gets the current file and corresponding lrepl and switch to that ns in that lrepl
- tab
  - an app that knows ho to append data
  - shows logical repls as a line in the top, active repl is underlined
  - repls which connections are off are grey,  otherwise color is green or smth
  - has a settings button (top-right) that shows a list of all connections (from mult.edn) with connect/disconnect buttons
  - divs
    - slim header shows lrepl
    - next printitng div that has scroll
    - input div (console):
      - should be a file with editing working as in any file (e.g. a tmp,per serssion file)
      - so that when typing into it you are actually editing code in an editor file

