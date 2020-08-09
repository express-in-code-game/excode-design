
# Death Star Game

## first things first: project and money

- these questions cause confusion, need to be addressed
    - should project be eventually moneytized ?
    - should those who create the game and scenarios be paid, should anybody 'make a living' from it ?
    - should there be an organization behind, some fund or pot or whatever (for donations and fundraising and stuff) ?
    - should events(tournaments) have prize pools ? If not, what should the system be ?
- the answer is **no**
- because 
    - this project will be sort of a children's palyground - children come and play and build stuff, no reason needed except for wanting to
    - it's not good for the value(quality) of the project
    - the point is to play games/events, enjoy competetion, be inspired by the project, commercialization will devalue the product(game and the scene)
    - companies eventually lose the ability to innovate and eventually gravitate to revenue/stability goals, which kills the freedom, simplicity and individual creativity so desperately needed for games and playing
    - the project should be completely any-money-concerns free space, no strings attached or hidden, openly created and contributed, for anyone to play
    - tournament system should be like other systems(groups, brackets, seasons etc.), except with points only, no prize money: playing games does not require any extra motivation, except for some bragging rights
    - it is also important to keep the development process (game and scenarios) creative and pressure-free, which will lead to better product

As Jesus says:

> <b>19 “Do not store up for yourselves treasures on earth, where moths and vermin destroy, and where thieves break in and steal. 20 But store up for yourselves treasures in heaven, where moths and vermin do not destroy, and where thieves do not break in and steal. 21 For where your treasure is, there your heart will be also.</b>

> <b>22 “The eye is the lamp of the body. If your eyes are healthy,[c] your whole body will be full of light. 23 But if your eyes are unhealthy,[d] your whole body will be full of darkness. If then the light within you is darkness, how great is that darkness!</b>

> <b>24 “No one can serve two masters. Either you will hate the one and love the other, or you will be devoted to the one and despise the other. You cannot serve both God and money.</b>

source: [NIV Bible, Matthew:6](https://www.biblica.com/bible/niv/matthew/6/)

## project in a nutshell

- what's the point of the project ?
    - create a game and play events(tournaments)
    - game should have a scene, should be on twitch and stuff
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
    - a scneario will typically have a resource space (what elements, APIs can be used) and a solution space (where the composion will be tested)
    - both spaces are randomly generated, but there will be no randomness afterwards, so that there will be no RNG-complaints
    - for example
        - a scenario where players should build drones to explore a planet
        - a resource space will contain elements/parts/devices to build from, solution space will be unique location on the planet
        - players define (in code) what there drones will be and program their behaviors
        - the system(drones or multiple, it's up to player what to build) will explore the map(planet) according to the program
        - the system that explores/achieves the most winss
        - no in-game RNG, players are in absolutely equal positions, yet competing
    - games should be configurable to be run in fixed time, for example 10-15 minutes, or maybe longer
    - players can evaluate code interactively in the REPL to explore resource and solution space
    - players can use an editor of their own choice
    - point is
        - players have a real language to express logic, not just clicks and hotkeys
        - scenarios should be designed not for fast-typer-wins, but for clearer-thinker-wins
- what about events(tournaments) ?
    - should be possible to host an event from your pc (schedule it via social networks)
    - players would add your IP/domain to their game client, connect and play
    - should be possible to select a tournamnet format and what scenarios will be used in which rounds
    - should be possible to export tournament configuration as a file (code or data), so that it can be shared via github
    - ideally, there should be a way to export data from self-hosted events to have global data stats (but that's not a priority)
    - later would be awesome to create a new type of volunteer cluster (cloud), so that computers can be volunterily added and system could run decentralized
- what programming language will this prject use ?
    - clojure and/or clojurescript
    - these are unmatched in terms of REPL, reach and features
    - language will be used as is, unmodified
- what will game's code editor look like ?
    - players will edit files using an editor of their choice
    - this way there is no wrapping or coupling
- this document is not very good presentation-wise. There are already lots and lots of games based on this idea, plus some more specialized (analogous to e-sports you might say) events, like ICFP and similar ones. How exactly is this different?
    - very true, it's a poor presentation; the point is to share and idea and find people who have the same problem/goals already
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

## links

- mailing list
    - https://groups.google.com/g/deathstargame
    - deathstargame@googlegroups.com

<br/>

- [thought process behind the game](https://github.com/express-in-code-game/lab.search-for-the-game/blob/master/docs/design.md#building-is-about-developing-a-language)
- [idea of a volunteer automated cluster](https://github.com/express-in-code-game/lab.origin-cluster/blob/master/docs/design.md)
- [some notes on events](https://github.com/express-in-code-game/lab.cloud-native-system/blob/master/docs/design.md#user-experience)

<br/>

- existing games that use languages
    - https://www.codingame.com/ide/puzzle/onboarding
        - https://github.com/CodinGame
    - https://screeps.com/
        - https://github.com/screeps