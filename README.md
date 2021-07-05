
# DeathStarGame

- the game, multiplayer, ever non-commercial money-free program

## ever free and non-commercial

- DeathStarGame is *ever any-money-concerns-free program*, openly created and contributed
- it's not a org, not a project, not a namespace - it's just a program
- no funds, no pots, purely volunteer creating process, no strings attached or hidden
- commercial insterests and "safety" income expectations make programming a routine, a drag, money suck freedom and excitement out of creation process, so desperately needed for making and playing games, and devalue the result
- DeathStarGame is a children's playground, an inspiration-driven development
- we are bulding a spacious, epic, designed for e-sports, yet elegant and simple game for players to perform, somehting Jesus would play

## program in a nutshell

- what's the point of the program ?
    - build a game for e-sport, for players to perform <s>and play events(tournaments)</s>
    - no noise and distructions - only the game to play
    - thinking over micro/mechanics: players use programming language to complete scenarios' objectives
    - designed for e-sports <s>users compete in tournaments</s>
    - DeathStarGame is exclusively a 1v1 game
    - <s>never stale: scenarios like extensions, can be installed via a link, each being a game of its own with its own ui</s>
    - <s>better scenarios overtime will become the standard for e-sports scene and tournaments</s>
    - should be one game, not many scenarios, a sport, designed for players to perform - one game, one of, like basketball, tennis, starcraft etc.
    - matches: like in tennis or big starcraft/aoe2 torunaments, or basketball - players should play a match a day, bo5/bo7, give a performance, and game should be 
    designed for players to perform, quality over quantity, a match is an event
    - game comes with no ladder - only host and connect
    - <s>peer to peer  global, decentralized
      - user computers (laptops) run the game
      - users can turn game on/off, yet it's a global program
      - the users currently online are the network
      - when node goes online, it fetches updates into local global db
      - when 1000 people participate in event, their machines will host games
      - like git and forks: if 8 peers are playing and host goes down, games continues
      - game is cold and efficient: the global program is formed by laptops running it only, that constantly go on and off
      - core mechanism - pubsub (gossip etc.) for network dataflow</s>
      - game is not global - users host and connect as usual, but through peer-to-peer network, program stores data locally only, on user computer it runs on
    - installation:
      - <s>peer node is installed in docker, use - browser on localhost:port
      - and no installation: users can just open browser and connect to one of the nodes, 100 nodes, 10000 users</s>
      - desktop program
- aren't there enough games already ?
    - ok, there were chess and go and stuff
    - then internet came about
    - then it all grew, and now we have a few scenes for different games to play, nice
    - but they are all click-focused and quite stale, only maps change
    - not enough events <s> no/few automated ways to create events and invite people, only 1v1s</s>
    - 90% of twitch streams are crap, because games are mostly uncomfortable or boring to play, so channels become stale
    - there is a culture of 'you can only play if click fast enough, before you body breaks down from poisonous crap-foods events advertise'
    - still, great games like Age of Empires, Starcraft have impressive scenes and are fun to watch
    - but we can do better by contributing to the world of games a new kind of games
- what kind of games ?
    - players use a programming language to build/create a composition/solution within a game <s>scenario</s>
    - <s>scenarios are created by people and are installable via a link (from github for example), best scenarios will become standard for tournaments
    - a scenario is like a game in itself with it's own idea and objectives</s>
    - <s>scenario is</s> maps are randomly generated at the start, but there will be no randomness afterwards, players are always in equal position, there will be no RNG-complaints
    - <s>for example
        - a scenario where players should build drones to explore a planet
        - a resource space will contain elements/parts/devices to build from, solution space will be unique location on the planet
        - players define (in code) what there drones will be and program their behaviors
        - the system(drones or multiple, it's up to player what to build) will explore the map(planet) according to the program
        - the system that explores/achieves the most winss
        - no in-game RNG, players are in absolutely equal positions, yet competing
    - games should be configurable to be run in fixed time, for example 10-15 minutes, or maybe longer</s>
    - players can evaluate code interactively in the REPL to explore resource and solution space
    - players have a real language to express logic, not just clicks and hotkeys
    - <s>scenarios</s> game should be designed not for fast-typer-wins, but for clearer-thinker-wins
- what about events(tournaments) ?
    - <s>should be possible to host a server, tournament on your laptop
    - should be possible to select a tournament format and what scenarios will be used in which rounds
    - should be a simple bracket, like in tennis or NBA playoffs, single elimintation - no double elimination crap, players meeting each other once, it's an event
    - tournaments are up for people, users will be able to create their own tournaments, everyone equal, like on github</s>
    - game is game only, no tournament creation
- there are already lots and lots of games based on this idea, plus some more specialized (analogous to e-sports you might say) events, like ICFP and similar ones. How exactly is this different?
    - please, share a link to such games!
    - for example 
        - this is https://liquipedia.net/starcraft2/Main_Page , probably the best resource for tournaments, and all the games there are usual suspects
        - or obviously https://www.twitch.tv/directory,  is there a category for such a game(s)?
    - and main point: it should not be a napkin-game, or 'look-ma-what-i-have-done'; it should be better than Blizzard does things, because it should be open source
    - and better than Microsoft, that just released Age of Empires 2: Definitive Edition and are investing into tournaments and game updates to catch up with Blizzard
    - not mentioning Dota, LoL , CS:GO and stuff like that
    - those are scenes, those are games; they gather 100 000 viewer streams on twitch
    - this game should be a much better game than those <s>do all that</s>, be open source <s>and not stale</s>
    - and not like Heroes of Might and Magic 3 scene on twitch, where some dudes got the source code, maintain a server and although do it for free, it is again, happening under the carpet <s> and again, all tournaments are organized via forums and happen rarely </s>
    - and existing e-sports games are closed source, most dark and click driven, most moneytized
    - hidden, hidden, private, hidden... so such games do not exist yet, otherwise we would have noticed it
- what programming lanugage will the game use?
    - clojure
- docs?
    - no, just the program and readme with goal and compile
- tests?
    - no
- runtime?
    - program's runtime is JVM, no GraalVM compilation
- gui?
    - <s>cljfx/cljfx</s> will stem from existing programs
- db?
    - replikativ/datahike