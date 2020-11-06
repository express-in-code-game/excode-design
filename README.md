
# DeathStarGame

- the game, multiplayer, ever non-commercial money-free project

## ever free and non-commercial

- DeathStarGame is *ever any-money-concerns-free project*, openly created and contributed
- no funds, no pots, purely volunteer creating process, no strings attached or hidden
- commercial insterests and "safety" income expectations make projects a routine, a drag, money suck freedom and excitement out of creation process and devalue the result
- DeathStarGame is a children's playground, an inspiration-driven development
- we are bulding a spacious, epic, designed for e-sports, yet elegant and simple game for people to be inspired, somehting Jesus would approve

## contents

- [project in a nutshell](#project-in-a-nutshell)
- [CONTRIBUTING.md](./CONTRIBUTING.md)
- [mailing list](#mailing-list)
- [discussion](#discussion)
- [design](https://github.com/sergeiudris/deathstar.lab/blob/main/docs/design/design.md)
- [related projects](#related-projects)
- [previous repos](#previous-repos)
- [other links](#other-links)

## project in a nutshell

- what's the point of the project ?
    - build a game and play events(tournaments)
    - thinking over micro/mechanics: players use programming language to complete scenarios' objectives
    - designed for e-sports: users compete in tournaments
    - never stale: scenarios like extensions, can be installed via a link, each being a game of its own with its own ui
    - better scenarios overtime will become the standard for e-sports scene and tournaments
    - eventually, it should run on web 3.0 (peer-to-peer internet) as decentralized global app
- aren't there enough games already ?
    - ok, there were chess and go and stuff
    - then internet came about
    - then it all grew, and now we have a few scenes for different games to play, nice
    - but they are all click-focused and quite stale, only maps change
    - not enough events, no/few automated ways to create events and invite people, only 1v1s
    - 90% of twitch streams are crap, because games are mostly uncomfortable or boring to play, so channels become stale
    - there is a culture of 'you can only play if click fast enough, before you body breaks down from poisonous crap-foods events advertise'
    - still, great games like Age of Empires, Starcraft have impressive scenes and are fun to watch
    - but we can do better by contributing to the world of games a new kind of games
- rant: [games, sports and e-sports in 2020](https://github.com/sergeiudris/blog/blob/ce9244d4fe9bfd920e0f4423b2201014200a4831/posts/2020-10-27-rant-on-esports-and-idea-of-a-new-esport-game.md#rant-games-sports-and-e-sports-in-2020)
- rant: [amazing communities, cruel games, regionalization in e-sports despite Internet working with the speed of light, literally](https://github.com/sergeiudris/blog/blob/ce9244d4fe9bfd920e0f4423b2201014200a4831/posts/2020-10-27-rant-on-esports-and-idea-of-a-new-esport-game.md#rant-enjoying-watching-e-sports-on-twitchtv)
- rant: [e-sports games are about control and not about composing/buidling](https://github.com/sergeiudris/blog/blob/ce9244d4fe9bfd920e0f4423b2201014200a4831/posts/2020-10-27-rant-on-esports-and-idea-of-a-new-esport-game.md#rant-e-sports-games-are-about-control-and-not-about-composingbuidling)
- what kind of games ?
    - players use a programming language to build/create a composition/solution within a scenario
    - scenarios are created by people and are installable via a link (from github for example), best scenarios will become standard for tournaments
    - a scenario is like a game in itself with it's own idea and objectives
    - scenario is randomly generated at the start, but there will be no randomness afterwards, so that there will be no RNG-complaints
    - [game scenario ideas](https://github.com/sergeiudris/deathstar.lab/blob/c2231ab989d46aa056765d8190f0f4e0bad848c4/docs/search-for-the-game.md#examples-of-scenarios)
    - for example
        - a scenario where players should build drones to explore a planet
        - a resource space will contain elements/parts/devices to build from, solution space will be unique location on the planet
        - players define (in code) what there drones will be and program their behaviors
        - the system(drones or multiple, it's up to player what to build) will explore the map(planet) according to the program
        - the system that explores/achieves the most winss
        - no in-game RNG, players are in absolutely equal positions, yet competing
    - games should be configurable to be run in fixed time, for example 10-15 minutes, or maybe longer
    - players can evaluate code interactively in the REPL to explore resource and solution space
    - players have a real language to express logic, not just clicks and hotkeys
    - scenarios should be designed not for fast-typer-wins, but for clearer-thinker-wins
- why use programming language as a gameplay?
    - complicated: [thinking turn-based game like HoMM3, realizing how limiting just one game world is, arriving at programming](https://github.com/sergeiudris/blog/blob/ce9244d4fe9bfd920e0f4423b2201014200a4831/posts/2020-10-27-rant-on-esports-and-idea-of-a-new-esport-game.md#considering-new-homm3-like-game-but-about-buildingcomposing-of-elements-and-github-like-system-around-it)
    - [original notes on why use language as the gameplay](https://github.com/sergeiudris/deathstar.lab/blob/c2231ab989d46aa056765d8190f0f4e0bad848c4/docs/search-for-the-game.md#building-is-about-developing-a-language)
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
- what about events(tournaments) ?
    - should be possible to host a server, tournament on your laptop
    - should be possible to select a tournament format and what scenarios will be used in which rounds
    - later would be awesome to create a new type of volunteer cluster (cloud), so that computers can be volunterily added and system could run decentralized
        - a global decentralized app on web 3.0 IPFS ..
    - when the system is global
        - there will be official non-commercial tornament system (seasons, points, seasonal grand finals etc.), so we can enjoy high qulaity pure competetion on a global scale
        - torunaments within the official system will be featured first and taged accordingly
        - elsewise users will be able to create their own tournaments
- there are already lots and lots of games based on this idea, plus some more specialized (analogous to e-sports you might say) events, like ICFP and similar ones. How exactly is this different?
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
- what programming lanugage will the game use?
    - clojure, because https://github.com/cljctools/readme#rationale
- what will the system look like?
    - [design](https://github.com/sergeiudris/deathstar.lab/blob/main/docs/design/design.md)

## mailing list

- https://groups.google.com/g/deathstargame
- deathstargame@googlegroups.com

## discussion

- reddit
    - https://www.reddit.com/r/Clojure/comments/hujrnk/pitch_lets_make_a_noncommercial_expressincode/
    - https://www.reddit.com/r/programming/comments/j8ez9g/currently_starting_a_new_project_ever/
    - https://www.reddit.com/r/Clojure/comments/j82vbd/currently_starting_a_new_project_ever/
- clojure mailing list
    - https://groups.google.com/g/clojure/c/3jT7HXR435g

## related projects

- https://github.com/cljctools
- https://github.com/ipfs

## previous repos

- https://github.com/sergeiudris/starnet
- https://github.com/sergeiudris/search-for-the-game
- https://github.com/sergeiudris/weekend-2020-07-23
- https://github.com/sergeiudris/deathstar.ltee
- https://github.com/sergeiudris/deathstar.play-scenarios-in-browser
- https://github.com/sergeiudris/youtube-channel

## other links

- [notes on game tournaments, events](https://github.com/sergeiudris/deathstar.lab/blob/c2231ab989d46aa056765d8190f0f4e0bad848c4/docs/cloud-native-system/design.md#user-experience)
- [figuring out what the game should be](https://github.com/sergeiudris/deathstar.lab/blob/c2231ab989d46aa056765d8190f0f4e0bad848c4/docs/search-for-the-game.md#building-is-about-developing-a-language)
- [project name](https://github.com/DeathStarGame/DeathStarGame/tree/95d6314d88f78ecaa2c4fe42f139b33f6033c4d8/project-name.md)
- [idea of a volunteer automated cluster before discovering IPFS](https://github.com/sergeiudris/deathstar.lab/blob/c2231ab989d46aa056765d8190f0f4e0bad848c4/docs/origin-cluster/origin-cluster.md)
- [why project does need no YouTube channel](https://github.com/DeathStarGame/DeathStarGame/commit/c712c6cea43a2ad4f9579f529ea29bc14f38f793)

#### existing games that use languages

- https://www.codingame.com/ide/puzzle/onboarding
    - https://github.com/CodinGame
- https://screeps.com/
    - https://github.com/screeps
- CyberCode Online
    - https://www.reddit.com/r/typescript/comments/ik0fxh/cybercode_online_a_mmorpg_webgame_that_looks_like/
- GLADIABOTS__AI_Combat_Arena
    - https://store.steampowered.com/app/871930/GLADIABOTS__AI_Combat_Arena/