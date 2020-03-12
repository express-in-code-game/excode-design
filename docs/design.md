
## game

#### other games observations

  - homm3 
    - AI battles are repetetive, needed as the role of combining is little: focus mostly on collecting
    - graphics is a still dark and eyestraining
    - simulteneous turns, mirror templates are genius
  - aoe, starcraft
    - micro heavy
  - hearthstone battlegrounds
    - mode is brilliant
  - terraria
    - softer graphics, but still dark
    - amazing idea and design
 
#### lore

  - defend starnet, one shot opportunity of preventing launch of netconrol (controldrone)
  - research, change, build, balance a missiondrone and a team (a hero and research drones)
    - the winner's missiondrone and team will take on the mission of protecting startnet from netcontrol's takeover
  - planets & teleports, remote controlled drones, ships
  - single map
  - start: teleport to the map (a planet)
  - players play for competing/fighting team, both thinking their missiondrone and team will perform better than opponents, so better build wins
  - entities vary, sets, tags, combinations: code,warp drives, fields, elements, fruit, self orient time .. etc.
  - characters, reserach drones start from 0
  
#### gameplay

- a tiled map
- player collects varios items, chooses different options, experiencing effects to build a better missionship
- game is values-transparent with little things to learn, provides calculations, more focuss on the picture and decisions, less arithmetics
- maps , items quantities and even qualities are randomized
- randomness on initial generation only
- optimal game time ~15-90 min

#### elements

  - goal is game completeness, balance, so complete set of entities, no bloating expansions
  - entities have tags(sets): gathering organic discovery etc.
  - droids
    - drive types 
    - research abilities
    - distance speed
  - combine parts compute fields drives etc.
  - research quality
  - compute capabilities
  - decision making accuracy, energy, vision, vitality

## how

- first, setup CD to the cloud - project must be live
  - fixed single server instance
    - run kafka stack and system stack separately
    - update the system via pull, up -d
    - ability to repl into production
  - aws
    - https://aws.amazon.com/ec2/instance-types/
    - https://aws.amazon.com/blogs/compute/amazon-ecs-and-docker-volume-drivers-amazon-ebs/
    - https://aws.amazon.com/ec2/pricing/reserved-instances/pricing/
    - https://aws.amazon.com/ebs/pricing/
  - build up to assets, deploy to aws, gen large seqs, sim gen daily
- keep it simple, data files and fn files, repetetive if needed
  - game is a seq of events
- use repl from the start as it's the most powerful design tool, inform the design
  - events, words, simple shapes; assets will form last
- consider figwheel main if it's less cpu consuming than shadow-cljs
- use DOM for the board, but keep it simple - details and info in windows/popups/panels
- file per asset, little to no shared code, an asset as sexp, use lib, render into canvas/svg/png
- gen assets into files, preload, set as sources to tiles
- if needed, run assets as code (render into svg/canvas) in enetity information window, 
- point is: assets are code, files are generated
- docs, anouncements, release notes: simple dirs with index.md containing links to file per posting
- https://github.com/localstack/localstack
- persist data in kafka (as event/record store)
- game state
  - the game is powerful, so is the client
  - game is not computed on the server
  - game state is persisted on the server in compact form as {:default-game-events [] :game-events []}
  - client being powerful, computes all the needed derived/queryable state for interactivity
    - derived state may be a map {:derived1 {} :derived2 {} ..}
    - on every event, relevant deriver fns are invoked (derived1 ctx evt), where ctx contains all refs
    - derived1 computes and updates :derived1 key in the map
    - also a derived-db may be used to store entities in in-memory db for querying with a proper lang e.g. datalog
  - if client tab is closed
    -  client reconnects
    -  server sends the compact state 
    -  client recomputes the game state as (apply next-state (into default-game-events game-events )) (recompute-derive-whaterver-is-needed ..)
  - if no disconnect, client receives only new game events and updates the game state
  - but server adds timestamps - so time is independent from the client
- assets
  - use s-expressions to gen svg
  - colors, lines, shapes (for cape, spheres, facilities, skills, fruit tree etc.)
  - sound effect and better assets will grow

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

## drawing board

- ignite
  - kafka
  - the core of the system via repl interface
  - game
  - http interface, ws
  - ui
  - iterate
- ui
  - home page simple routes: /events /games
  - /games : list of games or create a gam
- search
  -  used only for events, games, users
- data and secutiry
  - user account data only exists in user.data
  - if user deletes their account, it gets removed from user.data (kafka tombstone event)
  - in the system (event brackets, stats etc.) it get's shown as 'unknown' (only uuid is used in other topics)
  - only events history, event placements, user wins/losses are persisted, not all games
  - user identity as email into uuid
  - after 0.1 add token, https, wss
- v0.1 ux
  - users creates a game -> game.data
    - user presses create a game from /u/games list
    - once response/event arrives, list gets updated
    - entry (game) has open button
    - on click, tabs open with the current state of the game
  - user changes settings of the game -> game.data
  - once finished, user presses 'invite' or 'game ready' or 'open' -> game.data game becomes visible in the list and joinable
  - opponent joins ( if rating >= specified by the host in settings) -> game.data
  - more settings, bans, both press 'ready' -> game.data
  - host presses 'start the game' -> game.data
  - all ingame events are sent through ingame.events topic
  - if user closes the tab, they can reopen it from 'ongoing games' list -> get current state snapshots from game.data and ingame.events
  - after the game has started, host can't cancel it
- v0.1 gameplay
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

