
## what

- Starnet
  - a board-like game
  - with a server and regular events(tournaments)
  - complete, free and open to everyone
  - to be worthy of a scene and community around

## on games in general

- games are fundamentally magic, one of the better ways for beings to interact
- games are being 'produced', but suprisingly, there are no events to play
  - most games are micro heavy, dark, including the classics
  - monetization noise is unhelathy and disturbing
  - events(tournaments) are rare, not for everyone, brackets are on separate services
  - prolifiration of the idea that the player must be an unhealthy aggry yob, seeking for life-replacing occupation that is also 10-hour-a-day-or-bust
  - force into one mindset: compete for resources, collect more, bring opponent down
- there is a glaring need for games that are
  - simplier, lighter, have no micro
  - have a map (board)
  - are a service, can be played/observed in a browser, events and brackets included
  - are complex and interesting enough to be worhty of a community around
  - are moneytization free, come as whole available for everyone, no forced 'if you don't play, you have limit access to updates'

## observations

- HoMM3
  - AI battles are repetetive
  - little role of combining enetities
  - simulteneous turns save the day
- AoE, Starcraft
  - micro heavy
- Hearthstone Battlegrounds
  - mode is brilliant
- Terraria
  - softer graphics, but still dark
  - amazing idea and design

## user experience

- events (tournaments, matches) are at the center, home page contains official events and most upvoted
  - games tend to have reasonable duration time, so most events have estimatable start and finish time, they can be  planned for
  - event rounds start at X, no waiting for the opponent
  - official events start at X, not when full
  - users can create events (invite only or public), unset time constraints
- players have rating, rating can be reset
- all games are public and can be observed
- the game opens in a tab, so can be always reopened/reconnected
- users can
  - press 'find an opponent' and be auto matched against most equal opponent (or can decline)
  - enter an event
  - create/configure an open/invite-only  event
  - browse events (with a filter)
  - join a game
  - create an open/invite-only game (no title, match description has a format)
  - browse open games (with a filter)
  - create lists of
    - other users
    - events
    - users and events
    - as there will be no limitations like 'add to friends' 'follow' etc. - user creates their own lookup lists
  - export (to load, exahnge) game as data
    - load can be a url to a file (edn or transit, possibly edn for readabiity)
- no chat
- user profile has rating and event results (can be reset)
- official and community map templates and configurable events
- pre game bans, selections
- observing
  - select templates (simple detailed) for game info/stats [templates are components]
  - toggle visibility of a player's entities to better see the position of one player
- tournament time frames
  - tounaments preferably have estimatable, evening/a day timeframe
    - once signup ends, bracket is form, countdown to round 1 begins
    - player sees 'upcoming match in X:X;
    - game starts automatically or player can decline and take a default loss
    - once round ends, next countdown begins
    - players can see the brackets and schedule
  - tounaments can be configured to have natuaral timeframe
    - all standard
    - to observe or play the game user clicks the match in the bracket, gets into the game lobby or game itself
    - players can mutually agree to nullify the result and re-play
- game history and replays
  - after game is fninished, game data is trasacted to db
  - players can browse game history and watch replays
  - games are also persisted in clients filesystem, can load a replay via a file

## game world

- defend starnet, one shot opportunity of preventing launch of netconrol (controldrone)
- research, change, build, balance a missiondrone and a team (a hero and research drones)
  - the winner's missiondrone and team will take on the mission of protecting startnet from netcontrol's takeover
- planets & teleports, remote controlled drones, ships
- single map
- start: teleport to the map (a planet)
- players play for competing/fighting team, both thinking their missiondrone and team will perform better than opponents, so better build wins
- entities vary, sets, tags, combinations: code,warp drives, fields, elements, fruit, self orient time .. etc.
- characters, reserach drones start from 0

## game features

- a tiled map
- player collects varios items, chooses different options, experiencing effects to build a better missionship
- game is values-transparent with little things to learn, provides calculations, more focuss on the picture and decisions, less arithmetics
- maps , items quantities and even qualities are randomized
- randomness on initial generation only
- optimal game time ~15-90 min

## game completeness

- game completeness, balance, so complete set of entities, no bloating expansions
  - entities have tags(sets): gathering organic discovery etc.
  - droids
    - drive types 
    - research abilities
    - distance speed
  - combine parts compute fields drives etc.
  - research quality
  - compute capabilities
  - decision making accuracy, energy, vision, vitality

## gameplay

- players (azure and orange) start on the map, charachter is represented with a cape
- 1 hero, 3 research drones (represented with a colored sphere)
- missiondrone is represented with a large sphere (possible made of nanites) and orbiting supporting spheredrones
- map is visible and open to both opponents equally, 128x128
- players are not competing for resources, what one can get, the other can as well
- players choose their initial position on the map 
- research, collect, rebalance, by roaming the map
- players choose what to visit and collect, make choices, balance the missiondrone's and the team's characterisitics
- players see each others moves, but not choices (the missiondrone build, team's stats etc.)
- player can visit a fruit tree to improve certain skills of a hero, for example, or visit and select nanite modules for the missiondrone
- players have limited moves per day, limited days (say, 7)
- total time for 7 days - 15min
- every 5 mins there is an battle simulation, 30sec per move, 3rd battle is final
- in the battle goal is to disable(defeat) opponent's mission drone
- hero and research drones are engaged and support (they also develop skills and abilities)
- no distance
  - movement in terms of energy spent at places, maybe increasable limit on how many places can be visited
  - visiting a lot should not matter though 
- missiondrone attributes
  - compute capabilities
  - data bank
  - knowledge bank
  - independent agent programs (for unpredictability)
  - fields
  - networking range
  - design quility
  - human interface simplicity
  - abstraction level (like an age in AoE)
  - defensive/offensive resources
  - energy
  - ...
- hero
  - accuracy
  - decisionmaking
  - resolve
  - drone design 
  - reach (movement)
  - creativity(ideas)
  - endurance (energy)
  - vision
- reserach drone
  - reach
  - reserach capabilites
  - absctraction level
  - fields
  - hull
  - energy
  - hoisting (carrying) capacity

## assets

- use s-expressions to gen svg
- colors, lines, shapes (for cape, spheres, facilities, skills, fruit tree etc.)
- use DOM for the board, but keep it simple - details and info in windows/popups/panels
- file per asset, little to no shared code, an asset as sexp, use lib, render into canvas/svg/png
- gen assets into files, preload, set as sources to tiles
- if needed, run assets as code (render into svg/canvas) in enetity information window, 
- point is: assets are code, files are generated
- but first: events, words, simple shapes; assets will form last

## documentation

- github repo with .md files
- docs, anouncements, release notes: simple dirs with index.md containing links to file per posting


## system design

- CPS communicating sequential processes
  - https://www.infoq.com/presentations/clojure-core-async/
    - "function chains are poor machines
    - "good programs should be made out of processes and queues
    - "the 'api du jours' events/calbacks - Definition of du jour. 1 : made for a particular day
    - "external flow state
  - https://clojure.org/news/2013/06/28/clojure-clore-async-channels
  - https://github.com/clojure/core.async
- datastore soultion
  - allows connections from multiple apps, querying, searching
  - options: datomic dgraph
- routing, https
  - traefik
- on data
  - clients emit events
  - server persists minimal state (raw events) in memory, acts as coordinator or/and broadcaster
  - clients compute state
  - after game is finished, server trasacts game data to database, players can browse history
  - games are autosaveed on the client as well: to file system via browser extension
  - if server goes down, game can be restored from save files

## identity

- ideally should be implemented via providers (github google twitch ..)
  - system apps should be completely unaware of identity process, only receiving user's identity (or key to get it) along with request
  - signup, singin, change of password, auth, authz, web interface, support existing providers -  should be abstracted into a service (with plugins and/or client)
  - all existing names in different provider domains should be already reserved for users of the new system
    - to avoid name collisions provider domain should be used to fully qualify a name
    - e.g. github.com/user1 and google.com/user1 may belong to different users, but should be unique in the system
  - users should be able to link accounts, merge accounts - history etc., name of the account is one of the merged
  - system is free to have its own user abstraction and data, with identity being handled by a decoupled layer
- however, as of now, exising providers auth is not yet automated
  - you have to manually (via provider's web interface) register apps, provide urls and use keys
  - unlike let's encrypt project, which automates tls
- implementing your own custom identity (not oauth) is a step back and a waste
- considering all that
  - system should run its own oauth provider service and identoty service as the client
  - the resulting layer should be self-contained and decoupled from the system, may have its own db
  - existing providers can be added later when proper tooling(automation) is created

- agent user
  - system has the user abstraction
  - identity is resolved in the identity layer (via identity apps e.g. ory) and mapped to the user, so user abstraction is very loosely (almost de-) coupled from identity
  - the identity layer follows oauth, meeting the standards: user can create an account or login with providers
  - system also has agent identity: it  allows to create a user and use the system
  - creation requires no input, returns a token and user data with a random name
  - agent user is equal to any other user; if token is lost, a person can create another agent user
  - agent users can be used at any time, they are fundamental to the system
  - data behind agent user is not persistent long term, but can be relevant within a week/month etc.
    - e.g. you can create an agent user and use the system until token is in your browser's localStorage
    - or you may want to create agent users daily or even more frequently
    - until the token is in your browser and active
  - system will develop mechanisms to expire agent accounts and clear data once the identity layer is operational
  - but: agent users will remain a way to use the system



## notes on implementation

- queues and processes
- protocols
- simplicity: bad abstractions are worse than no abstractions
- a process (service) may have its own runtime (container) 
- app = process, apps are smart
- connections and channels are global defined (on top of service's main), explicit
- development through traefik(http) and docker-compose
- lein + deps with lein-deps-plugin
- apps know database and http, no communication queue required
- game can be played (state-wise) from cljc repl
- consider electron