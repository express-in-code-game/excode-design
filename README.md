
# DeathStarGame

- the game, multiplayer, ever non-commercial money-free project

## ever free and non-commercial

- DeathStarGame is *ever any-money-concerns-free project*, openly created and contributed
- no funds, no pots, purely volunteer creating process, no strings attached or hidden
- commercial insterests and "safety" income expectations make projects a routine, a drag, money suck freedom and excitement out of creation process, so desperately needed for making and playing games, and devalue the result
- DeathStarGame is a children's playground, an inspiration-driven development
- we are bulding a spacious, epic, designed for e-sports, yet elegant and simple game for people to be inspired, somehting Jesus would approve

## contents

- [project in a nutshell](#project-in-a-nutshell)
- [development](#development)

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
- what kind of games ?
    - players use a programming language to build/create a composition/solution within a scenario
    - scenarios are created by people and are installable via a link (from github for example), best scenarios will become standard for tournaments
    - a scenario is like a game in itself with it's own idea and objectives
    - scenario is randomly generated at the start, but there will be no randomness afterwards, so that there will be no RNG-complaints
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
    - please, share a link to such games!
    - for example 
        - this is https://liquipedia.net/starcraft2/Main_Page , probably the best resource for tournaments, and all the games there are usual suspects
        - or obviously https://www.twitch.tv/directory,  is there a category for such a game(s)?
    - and main point: it should not be a napkin-game, or 'look-ma-what-i-have-done'; it should be better than Blizzard does things, because it should be open source
    - and better than Microsoft, that just released Age of Empires 2: Definitive Edition and are investing into tournaments and game updates to catch up with Blizzard
    - not mentioning Dota, LoL , CS:GO and stuff like that
    - those are scenes, those are games; they gather 100 000 viewer streams on twitch
    - this game should do all that, but be open source and not stale
    - and not like Heroes of Might and Magic 3 scene on twitch, where some dudes got the source code, maintain a server and although do it for free, it is again, happening under the carpet, and again, all tournaments are organized via forums and happen rarely
    - and existing e-sports games are closed source, most dark and click driven, most moneytized
    - hidden, hidden, private, hidden... so such games do not exist yet, otherwise we would have noticed it
- what programming lanugage will the game use?
    - clojure

## development

- requires `git`, `nodejs`, `java` (11+), `docker`
```shell
git clone https://github.com/DeathStarGame/DeathStarGame
cd DeathStarGame/ui
bash f dev # starts shadow-cljs dev server for ui
cd DeathStarGame/app
lein repl # REPL into app
```
- DeathStarGame icon will be in app tray, click it to `quit`
- UI on http://localhost:9500 or 3080
- to make an uberjar `bash f uberjar`


