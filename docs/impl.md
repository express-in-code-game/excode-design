
- traefik
  - gateway to the system
  - uses system.auth app via ForwardAuth to authenticate and when possible authorize requests
  - system.gamehub does it own authorization (e.g. whether or not user can get gamesate) based on in-memeory game state

- system.iap
  - identity and access proxy
  - uses buddy to authenticate, attaches user data to request, so apps get user (e.g. System-Identity header)
  - use (pr-str) for claims :val so uuid is properly written/read

- system.api
  - provides namespaces, that expose system operations (fns of args) to all processes of the system. runtime agnostic
  - all operations return channels
  - system.api.dgraph
    - dgraph impl of txs and queries

- cljctools.dgraph-client
  - provides unified protocol for dgraph
  - 2 implementaitons (for jvm and for browser)

- system.gamehub
  - responsible for socket connections and game states

- db clients (connections)
  - a connection should be a process, exposing channels
  - when (def db-client (db/create-client {})) an interface should be returned
  - process starts, and tries to connect/reconnect, but should not fail unless opts define max-reconnect attempts, then throw
  - when api fn is called with the conn as arg, it should pt that call onto a channel, again, with timeout, and return an out channel
  - so that connection is refereable(a var) an yet is completely async

- /settings
  - an edn data structure containing all settings available for the user
  - editor is user for editing, no forms

- routes
  - game/:uuid 
    - handled by gamehub app 
    - when game is live, gamehub merges local state to db query result
  - stats/:username/game-history
    - returns the list of user games
    - page may support additional query filters
    - user can change settings to e.g. :show-all or :show-sets [:set-a :set-b :set-c]
      - when request for hsitory happens, user settings are retrieved and passed as args to game-history operation

- eviction
  - all data is removed
  - show as :deleted in graph (brackets, game hsitory etc.) where user data was used (as it does not exist any more)
  - ability to restore account within a week/month after delete initiation

- agents
  - are processes running in agents app (later in ui)
  - use the system by putting ops on channels
  - agents can play the game

- access to the game (authz)
  - to allow application of tools for http based authorization, socket connections should be decoupled from join/leave game logic
    - user joining or leaving the game is an http request and/or db operation
    - users that joined the game are queryable, and thus, authorization app is decoupled from gamehub
    - when user disconnects, it is shown in the ui (both setup and game), but game is unaffected - user can reconnect

- genral app(service) structure: http api as a layer
  - app is a process, that has an api/interface (as a library)
  - http interface is a layer (namespace), that uses base api to perform operations
  - namesapces: app.main app.api app.http
  - app may add a different interface as a layer, preserving core api

- websockets
  - api.http layer of an app handles sockets
  - uses procs (app) api to convey data via channels
  - so from app's perspective, there are only channels and api

- server side rendering vs client side rendering (SSR vs CSR)
  - SSR puts additional load on the server in exhange for
    - faster load time
    - search engines
  - SSR is better, indeed
  - however, opt for CSR
    - orient towards search engines supporting js and optimizing code for that
    - provide proper robots.txt and sitemap.xml
    - pre-rendering
      - generating html files has no benefits: loading htmls is slower than client side routing
    - CSR is fastest, execpt for initial load
      - solve via code splitting into dymanic loading (e.g. webpack shadow-cljs)

- code splitting
  - https://shadow-cljs.github.io/docs/UsersGuide.html#CodeSplitting
  - https://github.com/thheller/code-splitting-clojurescript
  - https://github.com/shadow-cljs/examples/tree/master/code-split
  - https://code.thheller.com/blog/shadow-cljs/2019/03/03/code-splitting-clojurescript.html

- UI
  - UI should be an app with extenions
  - route is a process
    - navigating is alike an editor's a command that activates/deactivates extensions
    - rendering the page may or may not be what extension does, it's up to the extension
  - extensions use UI's api
  - ui may preload a template to render from an extension
  - but it's extension  that loads data, renders content; it's a process, an app within app
  - 'sign in/sign out with' and other potential menus can also be extensions
  - iden reading and ops are part of api
  - routes 
    - routes represent namespaces that contain operations for user to perform in the system
    - e.g. /game/:uuid route allows use to perform game ops (play the game)
    - or /stats/rankings page allows user to see the data and perform further querying ops
    - so route is a namespace, a process, an app, handled by one of the extentions
    - 404 page is also an extension, handling not-found-by-router routes
  - router puts vals on channel of what the route is, extensions activate/deactivate themselves (processes take from chan)
  - channels
    - extension expose channels (mults), so other extenions can tap if needed
    - extension api though is available to host only (activate deactivate ..)
    - extensions provide api for the channels (no deps) and maybe read-only direct api (doubtful)
  - extenion api
    - vsode example of accessing ext api
      - https://stackoverflow.com/questions/49719436/can-an-extension-require-other-extensions-and-call-functions-from-them
  - define and expose extension channels once (def channels ..)
  - extensions should be dynamically loadable (for better abstraction)

- observing
  - consider using server-sent-events for observers, sockets for participants


- developing cluster
  - nodes run in containers
  - develop using localhost and HA-cluster of container nodes (same node containers  can be run on other machines as well)
  - use localost browser ui, should be same as using sub.domain.io
  - if it's possible, consider installing cluster certificates dynamically using ?extension (it would allow https requests to control nodes)

- master nodes
  - cluster auto handles master node promotion/demotion
  - there can be from 1 up to 7 master nodes
    - https://docs.docker.com/engine/swarm/how-swarm-mode-works/nodes/
  - what's important: at any point in time master has at least 1 master node

- cluster spec/config
  - clsuter spec/config should be in files, version controled

- controlling the cluster
  - certain (e.g. cluster control) operations require security keys
  - keys are stored in a private repo and used during image builds
  - once origin image is built, cluster can be stared (originate) anywhere (at any node) using the origin image