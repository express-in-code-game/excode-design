# lab.weekend-2020-07-23

- invited a friend for a visit next week, to critique and debate the idea of such a game
- naturally I feel dared!
- so self-generated childish challenge accepted: make a version of a game to play when friend arrives, so he could realize the fututre of this kind of games

## rationale

- none :)

## design

- what should happen when friend arrives
    - friend brings his laptop, we install him an editor
    - we clone the repo and start the game from code on his laptop and mine
    - then connect and play a scenario, after which he concedes and proclaims this kind of games 'great' :)
    - a scenario must be fun, yet dead simple, such that a person with no knowledge of the language could learn in 5 min how to play this scenario
- what to avoid
    - solving f-ing fundamental problems
    - "to build this on a weekend, stupid-simple it kept must be" Yoda, 2020
- any rational reasons why this will work ?
    - I've got clojure, clojurescript and core.async, so I can build anything

## notes

- walkthrough 1
    - make cljs browser app (from previous labs, comprised of extensions, that uses antd) work in electron
    - so it was possible to navigate and add extensions(= views), so now it's like browser app but in electron
    - now read/write files to a dir (from settings.edn file, all constants are in settings)
    - so electron opens, reads a dir, gets settings, reads other stuff and so the client has started
    - networking
        - to avoid nodejs and npm, clojure server must be used
        - so client will connect to such server, which will transmit data
        - obviously, server is started in docker container on one of the laptops
        - sounds like beyond weekend ? but peer-to-peer won't be easier or am I wrong?
        - nah, that's correct
            - client starts, can load scenarios, kind of works, but without connectivity/tournaments, which require server connection
            - now, server should do the least in terms of scenarios: only transmit events
            - brackets and wiki are fundamental, so not now, do only PvP games or maybe a simple bracket tournament
    - identity
        - don't get me started; right now implement something in stupidest possible way
    - so what are the parts
        - client (electron app)
        - server (one jvm app, no nothing, no reverse-proxy, security etc.)
        - editors are editors, already done
    - so what happens
        - client starts, reads files, gets going
        - player inputs server address, client connects via socket
        - player creates a game via http request ? db ? lol, no; lol, yes, as funny as it sounds; peer-to-peer same amount of mess anyway
        - another player connects to server, sees the game, joins
        - palyer starts, files are read periodically accroding to scenario
        - electron app will expose api to scenario; scenario will be just a namespace (not external, git loadable yet, part of the weekend implementation)
        - so that scenario namespace will use fns like 'read-palyer-files' and stuff, 'send-some-data' or whatever; point is: as api
        - so when simulation starts, game gets what player has written, and uses that code in addition to scenario's to run somehting on screen
        - anyway, players score points or whatever, so evetually winner is decided
        - game over
    - how about
        - screw db: no need for the weekend two much mess with schemas and stuff

## links

- 
