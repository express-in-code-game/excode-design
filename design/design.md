
## gameplay

- other games observations
  - homm3 
    - AI battles are repetetive, needed as the role of combining is little: it's all about gathering
    - graphics is a still dark and eyestraining
    - simulteneous turns, mirror templates are genius
  - aoe, starcraft
    - micro heavy
  - hearthstone battlegrounds
    - mode is brilliant
  - terraria
    - softer graphics, but still dark
    - amazing idea and programmer-driven design
 
- lore:
  - the alliance is threatened by an enemy
  - two teams (players) are assembled to independently go through a number of levels and assemble a fleet
  - the better fleet will improve the alliance's chances of defending
  - like several teams of scientists assembled to solve a problem to provide a better solution
  - in the final battle simulation, the better fleet is determined
<br/><br/>

- a tiled map (one or more sectors) 

<img src="./assets/sectors1.png" /><img>

- sectors represent sectors (parts) of the alliance space/territory from where parst fo fleet (crew, ships etc.) can be assembled
- a player starts in a corner or side and aims to the exit
- player collects varios items, chooses different options, experiencing effects to build a better fleet
- players can always see all the sectors, can see oppnent moves, but not equipping/combining decisions
- maps , items quantities and even qualities are randomized
- randomness on initial generation only
- focus is on making decisions of what paths to take on the level, what to collect and what the combininig will result in
- no AI battles
- game is values-transparent with little things to learn, provides calculations, more focuss on the picture and decisions, less arithmetics
- player can see the whole map (one or more sectors) and can think ahead strategically, no unexplored/fog of war
- no turns mode:
  - time limit for the map or per sector (for example, 4 sectors 2-4 min each or the whole map in 8-16 minutes)
  - then turn based final battle simulation (with 5min chess clock)
  - so the game takes exactly 21 minutes, for example
- pre-battle test
  - say, every 2 mins or after every sector, players engage in a battle
  - maybe, like in hearthstone battlegrounds, automated
  - so both players get an idea of what can be improved
  - pre-battles results determine which player makes the first move in final battle simulation
- players can exchange/trade at any moment
  - player puts out an item and what they would like in exhange
  - oppoent either accepts, or puts an alternative price
  - automated, no dilaog, realtime at any moment  
- optimal game times ~15 ~30 45-60 60-90 min

## elements

- entities have tags(sets): complex organic discovery etc.
- use droids to collect/assemble in the sector
- droids
  - drive types 
  - research abilities
  - disttance speed
- combine ships parts compute fields drives etc.
- research quality
- compute capabilities
- decision making accuracy
- nanite types combinations
- within fleet vs fleet simulation
  - ship posistions
  - field coverage
  - shared compute network radii
  - transmission time
  - damage
- resources (basic and other elements)
- organic materials

## how

- first, setup CD to the cloud - project must be live
- keep it simple, data files and fn files, repetetive if needed
- use repl from the start as it's the most powerful design tool, inform the design
- consider figwheel main if it's less cpu consuming than shadow-cljs
- use DOM for the board, but keep it simple - details and info in windows/popups/panels
- file per asset, little to no shared code, an asset as sexp, use lib, render into canvas/svg/png
- gen assets into files, preload, set as sources to tiles
- if needed, run assets as code (render into svg/canvas) in enetity information window, 
- point is: assets are code, files are generated
- https://github.com/localstack/localstack
- docs, anouncements, release notes: simple dirs with index.md containing links to file per posting