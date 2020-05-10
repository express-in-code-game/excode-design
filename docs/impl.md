
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