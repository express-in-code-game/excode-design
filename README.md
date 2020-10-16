
# Death Star Game

- the game, multiplayer, ever non-commercial money-free project

## ever free and non-commercial

- Death Star game is *ever any-money-concerns-free project*, openly created and contributed
- no funds, no pots, purely volunteer creating process, no strings attached or hidden
- it's a playground, an inspiration-driven development
- we are bulding a spacious, epic, designed for e-sports, yet elegant and simple game for people to be inspired

As Jesus says:

> <b>19 “Do not store up for yourselves treasures on earth, where moths and vermin destroy, and where thieves break in and steal. 20 But store up for yourselves treasures in heaven, where moths and vermin do not destroy, and where thieves do not break in and steal. 21 For where your treasure is, there your heart will be also.</b>

> <b>22 “The eye is the lamp of the body. If your eyes are healthy,[c] your whole body will be full of light. 23 But if your eyes are unhealthy,[d] your whole body will be full of darkness. If then the light within you is darkness, how great is that darkness!</b>

> <b>24 “No one can serve two masters. Either you will hate the one and love the other, or you will be devoted to the one and despise the other. You cannot serve both God and money.</b>

source: [NIV Bible, Matthew:6](https://www.biblica.com/bible/niv/matthew/6/)

## what we are building

- a game system
- users compete in tournaments
- players use programming language to complete scenarios' objectives
- scenarios like extensions, can be installed via a link, each being a game of its own with its own ui
- better scenarios overtime will become the standard for e-sports scene and tournaments

## links

#### implementation

- https://github.com/DeathStarGame/deathstar
- https://github.com/cljctools (needed to create the game)
    - rationale https://github.com/cljctools/readme#rationale
    - source https://github.com/cljctools/cljctools

#### mailing list

- https://groups.google.com/g/deathstargame
- deathstargame@googlegroups.com

#### Youtube channel

- https://www.youtube.com/channel/UC4lYyonkvUGXNFcukJenKkA

#### discussion

- reddit
    - https://www.reddit.com/r/Clojure/comments/hujrnk/pitch_lets_make_a_noncommercial_expressincode/
    - https://www.reddit.com/r/programming/comments/j8ez9g/currently_starting_a_new_project_ever/
    - https://www.reddit.com/r/Clojure/comments/j82vbd/currently_starting_a_new_project_ever/
- clojure mailing list
    - https://groups.google.com/g/clojure/c/3jT7HXR435g

#### notes on game tournaments, events

- [./cloud-native-system/design.md#user-experience](./cloud-native-system/design.md#user-experience)

#### figuring out what the game should be

- [./search-for-the-game.md#building-is-about-developing-a-language](./search-for-the-game.md#building-is-about-developing-a-language)

#### idea of a volunteer automated cluster

- [./origin-cluster/origin-cluster.md](./origin-cluster/origin-cluster.md)

## rationale

#### aren't there enough games already ?

- ok, there were chess and go and stuff
- then internet came about
- then it all grew, and now we have a few scenes for different games to play, nice
- but they are all click-focused and quite stale, only maps change
- not enough events, no or few automated ways to create events and invite people, only 1v1s
- there is a culture of 'you can only play if click fast enough'
- still, great games like Age of Empires, Starcraft have impressive scenes and are/will be fun to watch
- but we can do better by contributing to the world of games a new kind of games


#### what's the gameplay ?

- players use a programming language to build/create a composition/solution within a scenario (program behaviour of entities on the map)
- scenarios are created by people and are installable via a link (from github for example), best scenarios will become standard for tournaments
- a scenario is like a game in itself with it's own idea and objectives
- for example
    - a scenario where players should build drones to explore a planet
    - a resource space will contain elements/parts/devices to build from, solution space will be unique location on the planet
    - players define (in code) what there drones will be and program their behaviors
    - the rovers will explore the map(planet) according to their program
    - the player that explores/achieves the most wins
    - no in-game RNG (only on generation), players are in absolutely equal positions, yet competing
- games should be configurable to be run in fixed time, for example 10-15 minutes, but it's up to scenario
- players can evaluate code interactively in the REPL to explore resource and solution space
- players have a real language to express logic, not just clicks and hotkeys
- scenarios should be designed not for fast-typer-wins, but for clearer-thinker-wins

#### there are already lots and lots of games based on this idea, plus some more specialized (analogous to e-sports you might say) events, like ICFP and similar ones. How exactly is this different?

- please, link me to such games if possible! I've been following e-sports for about 10 years and could not find any..
- for example 
    - this is https://liquipedia.net/starcraft2/Main_Page , probably the best resource for tournaments, and all the games there are usual suspects
    - or obviously https://www.twitch.tv/directory,  is there a category for such a game(s)?
- and main point: it should not be a napkin-game, or 'look-ma-what-i-have-done'; it should be better than Blizzard does things, because it should be open source
- and better than Microsoft, that just released Age of Empires 2: Definitive Edition and are investing into tournaments and game updates to catch up with Blizzard
- I'm not mentioning Dota, LoL , CS:GO and stuff like that
- those are scenes, those are games; they gather 100 000 viewer streams on twitch
- this game should do all that, but be open source and less stale
- and not like Heroes of Might and Magic 3 scene on twitch, where some dudes got the source code, maintain a server and although do it for free, it is again, happening under the carpet, and again, all tournaments are organized via forums and happen rarely
- and existing e-sports games are closed source, most dark and click driven, most moneytized
- hidden, hidden, private, hidden... so I don't think such game exists yet, otherwise we would have noticed it

#### what the system will look like?
 
- *correction: it should be designed and built as a decentralized distributed app of web 3.0 (ipfs libp2p ..)*
- the system will run in docker, with a browser ui
- no wheel reinvention whenever possible (should be most cases)
- ui is standard, mostly mutiplayer stuff, where users can create/join/observe games
- part of the screen will be for editor (editing clojure code), part will show scenario gui
- system (server) will handle game data, hisotry and user's game files (code), preferably in graph manner to be queried, but files maybe git or some other standard way to do it
- games are processes that will run on server (and scenario simulations): when needed, user's code from gui is sent to server, game simulation is run, new state is broadcasted to everyone
- identity: done as a layer, should be standard
- in general: the system should be done applying best practices, standards and protocols
- it is a standard system operating on user data, the special part is submitting code and running game sceanrios
- lets host a tournament
  - we install docker and run a single command that downloads docker images of the game and starts a docker deployment
  - we open a browser on localhost:port and see game ui
  - if it's the first time, we create admin account, otherwise we just login (data is persisted in a docker volume)
  - optionally we install additional scenarios via gui: go to github, find repos, copy-paste links into game's gui
  - we can play a couple of games by ourselves just to get to know scenarios
  - we invite others to our server: we open a port on our router and send our internet ip and port to others
  - they simply open their browser, type in ip:port, create their accout if the first time, and are redy to play
  - once everyone is on the server, we create an event(tournament), invite, configure (groups, brackets) and start the game
  - everyone sees an editor and scenrio's ui (a game map, scenario is a gmae of its own), periodically code is submitted and game state advances
  - the game duration is fixed (say, 10 or 15 mins, configurable), so nobody waits for nobody
  - one the game is over, scenario shows who one (most points or conquest or whatever)
  - those who lose, can observe remaining games
  - the tourney continues, someone wins and earns their bragging rights, can talk trash until the next tourney

#### the bigger picture behind using programming language for the game

- building is developing a language, sciences have evolving set of words(terms) and meaning expressed with that vocabulary
- it's sort of a libabry of functions, where some become deprecated  and new are added 
- and it is done mostly via an informal language (e.g. english), which allows us to record information and understand each other
- a formal, programming language, is aloso first and foremost for humans to talk to each other, describe our intentions(programs) and convey to others
- so it's for exhcange between beings first, the fact the computer can execute written programs is a desired sideeffect
- that's why the design of the programming language is so important: it is an abstractions used to express human thoughts, same as an informal language is
- so no matter how much automation and technology evolves, we will always use a programming language (formal or informal), to write programs/instructions
    - as an example, imagine thousands of years in the future giving a super-droid instrunctions verbaly, in an informal language, - "do this, create this, like that.." and nailing it, succeding
    - AI and deppleanring will be so evolved, machines will require any form of speech and it will suffice
    - but, if someone asks you "hey, share what you did with me", you can either give a recording of what you've said or ask a mahcine to generate some formal program from that
    - if it's recording, other people may not understand all what you say or interpret it differently from machine, there will be ambiguity
    - if it's somehting generated, it is exacly a formal language
    - but no matter what, it will be "hey, we need a protocol/formal languge/standards to express this and that, otherwise unclear"
- so programming languages will exist while any computation exists, no matter how automated
- and programming language should be elegant and inspiring, sane, like LISp
- along with that, we're using more and more droids and will be programming them
- the game is exactly that: people programming entities, droids, ships ... - scenario is up to the creator
- so that's fun and may even be inspirational, but fun and competetion/tournaments/streams first, Death Star is to e-sports, watch and enjoy 

#### what programming language will this game use ?

- clojure, because https://github.com/cljctools/readme#why-cljctools-and-why-use-clojure
