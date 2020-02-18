
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

  - defend starnet, one shot opportunity of preventing launch of netconrol
  - search change build a corecompute/coredrive/knowledgeframe and a team
  - planets & teleports, remote controlled drones, ships
  - single map
  - start: teleport to the map (a planet)
  - players play for competing/fighting team, both thinking their mission(drone/ship) and team (beings) will perform betterm so better build wins
  - entities vary, sets, tags, combinations: code,warp drives, fields, elements, fruit, self orient time .. etc.
  - characters drones start from 0
  
#### gameplay

  - a tiled map
  - player collects varios items, chooses different options, experiencing effects to build a better missionship
  - game is values-transparent with little things to learn, provides calculations, more focuss on the picture and decisions, less arithmetics
  - maps , items quantities and even qualities are randomized
  - randomness on initial generation only
  - optimal game time ~15-90 min

#### elements

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
  - build up to assets, purchase, deploy, gen large seqs, sim gen daily
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
- persist data in kafka

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
- no chat
- user profile has rating and event results (can be reset)
- official and community map templates and configurable events
- pre game bans, selections
- observing
  - select templates (simple detailed) for game info/stats [templates are components]
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
  - run kafka
  - seq of users/sessions, invite to a game
  - a tab per player
  - empty squared board, players move a circle, end turn
  - first palyer to move the circle to the exit wins
  - home page simple routes: /events /games 
  - /games : list of games or create a game