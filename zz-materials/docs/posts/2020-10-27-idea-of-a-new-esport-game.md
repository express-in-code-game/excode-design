# github.com/DeathStarGame: idea of a new esport game

This is a raw, likely difficult to read post. Not even a post, more of a comments. But could be interesting (or maybe I'm just biased).  
It's a rant mostly. Basically, a one-evening spit-it-out story of going from "where are no-micro e-sports?" to "what games should be?" to going for buidling such a game.   
TL;DR: game will use [IPFS](ipfs.io) peer-to-peer network, players will use programming lang to program game enteties, users can create their own scenarios and install via a git link.  
"Is there somehting to try/check?" - No. Not yet. As of 2020-10-27, it is still an idea.    

## project

- https://github.com/DeathStarGame

## content

- [rant: games, sports and e-sports in 2020](#rant-games-sports-and-e-sports-in-2020)
- [rant: enjoying watching e-sports on twitch.tv](#rant-enjoying-watching-e-sports-on-twitchtv)
- [thinking what kind of games is missing](#thinking-what-kind-of-games-is-missing)
- [what would the actual software sysmtem look like? like github](#what-would-the-actual-software-sysmtem-look-like-like-github)
- [first instinct - turn-based game like Heroes of Might and Magic](#first-instinct---turn-based-game-like-heroes-of-might-and-magic)
- [rant: e-sports games are about control and not about composing/buidling](#rant-e-sports-games-are-about-control-and-not-about-composingbuidling)
- [considering new HoMM3-like game but about building/composing of elements and github-like system around it](#considering-new-homm3-like-game-but-about-buildingcomposing-of-elements-and-github-like-system-around-it)
- [what is buidling](#what-is-buidling)
- [tying-it-together-building-and-the-game](#tying-it-together-building-and-the-game)
- [original notes on why use language as the gameplay](https://github.com/DeathStarGame/DeathStarGameblob/c2231ab989d46aa056765d8190f0f4e0bad848c4/docs/search-for-the-game.md#building-is-about-developing-a-language)
- [game scenario ideas](https://github.com/DeathStarGame/DeathStarGameblob/c2231ab989d46aa056765d8190f0f4e0bad848c4/docs/search-for-the-game.md#examples-of-scenarios)
- [design sketch: peer-to-peer multiplayer game using IPFS network](https://github.com/DeathStarGame/DeathStarGameblob/b86c49d160ca729cd2ec9eeb66abbeaabf3a8577/docs/design/design.md)

## rant: games, sports and e-sports in 2020

- games are a thing, fundamentally
- playing real sports games (football, frisbee, basketball, hide and seek - whatever) or e-sports
- the difference is in an *opportunity to participate*: with real sports we're just viewers, or at best can play on a playgorund or in a minor league
- there is no global basketball league or foorball - only events every so many years, and only for pros
- because sports are "geographically bound", as we all were... before the miracle of Internet emerged
- I can't emphasize enough - especially with the possiblities we have in 2020 - what a wonder Internet is: we are no longer geographically bound, and it happens with the speed of light, literally
- that's why we have such marvellous world-level systems: google, youtube, wikipedia, github, twitch  - which are *instantaneously* searchable, communicatable, freely participatable globally
- yeah, this systems may have regional quirks, but from a user's perspective  - they are global
- adding the two - light-speed fast Internent and the idea of sports, competetion - we get e-sports: global planetary sports and tournaments that we can not only watch but have an option to *participate* 
- *participate*  on a planetary scale: can sign up, go up the ranks within a global tournament system, with best players competing in a seasonal finals tourney
- great - we have Internet, abundunce and continous growth of software, open source is thriving and leading, and we have e-sports allowing us to play causally, and/or compete, and/or watch
- awesome, surely, there are computer games and e-sports for all tastes...
- well, this is https://liquipedia.net/ - a wikipedia for e-sports events
- as of 2020-10-27 the categories on https://liquipedia.net/ are: 
    - Dota 2 
    - Counter-Strike 
    - PUBG 
    - Starcraft 2
    - Rocket League
    - VALORANT
    - Overwatch
    - Rainbow Six
    - Apex Legends
    - Brood War
    - Warcraft 3
    - League of Legends
    - Smash
    - Heathstone
    - Heroes
    - Artifact
- will add to this list a few more popular games from http://twitch.tv/directory/ 
    - Teamfight Tactics
- and oh yeah, new AAA RTS game has been announced 
    - [Frost Giant Studios RTS](https://www.reddit.com/r/Games/comments/jeronr/frost_giant_studios_new_studio_staffed_by/)
    - in this [talk](https://www.reddit.com/r/starcraft/comments/jfvwg6/new_aaa_rts_frost_giant_devs_answer_all_your/) they discuss whether or not their RTS will have 3 or 4 races..
- almost all games listed above are either (with the exception of turn-based games which are different)
    - RTS or RTS-like game demanding fast repetitive micro mechanics (mouse clicking) and screen switching, to be good - click-grind for hours or be trash
    - a shooter (open world or not) - again, micro (mouse-clicking), and the bigger e-sport is, the more it comes down to who better moves mouse and clicks
    - a card game with every conversation coming down to "well, good RNG, lucky" or "bad RNG, unlucky, othewise would've won"
    - "yeah, that player is awesome - have you seen how they jumped out from the corner and moved the weapon's aim pixel to opponents head pixel with their hand faster than the opponent"
    - "yeah, they've been griding.. what a player"
    - of course, real sports are similar and any game is about some form of repetition, but degrees differ: some are less than others, some are more versatile
    - real sports are by the way another example of games being too focused on "fastest" "highest" "strongest", instead of smartest/healthiest/thinking clearer
    - tastes differ, and good thing we have choice
    - except for e-sports - in 2020 we have none: click fast or watch
- in contrast, chess or go or any other classic board game
    - requires no micro, can be played by thinking
    - but, the gameplay is quite limiting - one map and units never change..for thousands of years
    - and these games gather much less viewers on twitch (compared to League of Legends of Starcraft 2 at it's peak, with 100 000 + viewers for a tournament)
- another problem - all those games limitied by design to micro-orientation - somehow "agreeed" to all be dark with heavy animation
    - with the exception of Heroes or Might and Magic 3 and Age of Empires 2 - one is turn based and maybe the best game so far made, another is quite light, because it was made in 20th century
- while any programmer knows - and again, programming on github is a sorf of lifetime-long creative game - the best editors are spacious and calm, without noisy elements, that's where we spend hours, like gamers
- so who and where decided that games should be dark-colored, light turned off, grind untill your eyes are tired or wrist hurts? oh, of course - market
- like in real sports - it's called a "copy-cat" league - companies seek revenue and copy what's popular, already proven, so it's total lack of what-should-be thinking
- another issue - all global e-sport games in 2020 are closed sourced (but this is inherent to google, github .. and all other useful systems)
- but - planet Earth has open source phenomenon, that people also tried to shut down in its infancy, and now is a global norm
- with one exception - we still build systems closed source for "security" reasons, but this seems to be an temporary step in hostory as well
- it just haven't dawned on us that systems as well as tools should be open source
- another problem: control, games are about control, not about composing/buidling
    - well, what makes RTS games like Starcraft so appealing? even though they are about control, they are about buidling (but repetetive to a fault)
    - we start with a few units, than they - using a rule inside a game - move and collect minerals and we put building and build units
    - it's a very limited, fixed, but a system
    - but: we don't have any composition, building capabilities, none
    - there no elements or laws to combine thing into things like in chemistry
    - there are games like Terraria that allow crafting, but that crafting is a specific to this game, somehting custom, somehting we need to learn
    - and later, we'll get to this moment again: control vs buidling/composing and what is buidling and how to make an e-sports out of it, still allowing for control
    - we'll stop looking at games as control this and that and instead will think about building from elements, using the knowledge of humanity for that, giving game entities beahvior rather than directions

## rant: enjoying watching e-sports on twitch.tv

- indeed, in 2020 twitch.tv is the place to watch game streams and tournaments
- personally, I've been watching and following Starcraft 2, Age of Empires 2 since 2011
- mostly, because of the tournament system: there are events, groups, brackets, global finals, players and casters - like in real sport
- in early 2000s we used to play AoE2, Starcarf Broodwar by connecting to each other by at that time the telephone modem
- but still, it was multiplayer over LAN, even team games with one player hosting and others "dialing" to connect
- later Couter-Strike 1.6 became a thing, with the smae idea of hosting a server and others joining
- although exciting, that still was playing on an island - no global scene
- the first real e-sport was probably Starcaft Broowdar, with some community hosted servers and ability in early 2000s to find opponents online from around the world 
- personally, I've only palyed LAN at that time and learned this stuff in 2010s while watching SC2 torurnaments
- in 2010s twitch and streaming emerged, SC2 was lauched and AoE2 got Voobly (a global server with ranking and everything)
- communities started growing (me included), and free nature of twitch (just open URL and watch the tournament) made all the difference
- Homestory Cup - twice-a-year SC2 tournament was one of the best things to watch.. on the planet, not just e-sports
- because of childish, friends gathering to play the game and joking and competeing - the atmosphere, the spirit of the tourney was great
- same with watching TheViper player AoE2 or Kripp play Hearthstone and its early tournaments  - was a radically different experience, open friendly environment
- but: I've never played
- as much as I love communities (people, players, events, competetion) and consider myself part of it, I hate games and find them unplayable unless total submergence
- hate micro and one type of gameplay, and repetetive nature of games in SC2 - same map, same actions, who exectues better - it's a legendary game but it is *cruel*
- AoE2 at least have random map generation, which in m books is a must
- moreover, SC2 from the beginnning - due to genre and its design around micro - has *regional servers*
- despite having *global Internet* and being able to bypass geography we are back to being *regionalized*
- we have Internet, global streaming, great communities of people wanting to play and compete, but the design of games drives us back into islands and mostly watching
- insane

## thinking what kind of games is missing

- we are standing on the shoulders of giants - Internet, Unix, games, e-sports.. - and that is remarkable, no need to reinvent the wheel
- networking and e-sports are a reality already, but the games are dark and micro heavy and regionalization is still a thing
- so what kinds of games is missing? 
- games that are think-focused, like chess, turn-based in nature, but turns should be simulteneous (like in Heroes 3 Hota, Teamfight Tactics or Heathstone)
- games where players think most of the time and act less, with nothing but basic micro, not more than navigating a web page
- games that are non-commercial open source game projects, made from inpiration and for inspiration, something Jesus would approve
- so many people in the world make indi games and post things like "look ma, I made a game on a weekend" and yet when it comes to building a real thing we regress into forming companies
- Linux kernel, the biggest operating system on the planet, started and still continues as people communicating over email
- of course, there are organizations around it, but still it's a wonder of the world - the patches are still sent over email, on the basis of trust, just as they were in the beggining
- games that are open source, free, created non-commercailly by willing contributors - if creation is a playground, the game will stay light and dreamy
- games where it's not always/mandatory to compete for scarse resources and win by takeover - it's a limiting mindset
- since when it became mandatory that every game is always two parties denying each other reosurces and space? yet most games force us into spending an evening putting opponent down 
- map makers for Hearoes of Might and Magic 3 Hota realized that and introduced mirror map templates - players are on equal mirrored maps and collide after, so no "worse map position/generaion" whining or fighting for resource, as game comes down to who does better with what's given
- Hearthstone intorduced Battlegrounds mode (similar to Teamfight tactics) - players are able to build their composition over the course of the game, so better builder wins (but alas, heavy RNG game)
- even Age of Empires 2 have other win conditions besides conquest - who builds a better economy, who get's first to building a wonder - but those are never used in tournaments, because boring
- well, Hearthstone and Teamfight shows, buidling over the course of the game having abundunt/same resources can be made interesting, it's a design choice
- games that are by design not just about control and domination, but building, composing of smaller elements
- for example, in Terraria game players can craft elements from other elements, but it non-generic, a custom rules of this custom game world..
- whereas in Heroes of Might and Magic 3, the only compsable enteties are artifacts, very specific, not systemic, just a custom "this exact 3 things combine into exaclty this thing"
- if we want players to enjoy buidling, the game should have a system for that.. (we'll get to that later)
- and all existing e-sports game ar about the worst version of control: just move stuff with mouse from point to point as you're told to
- games that are truely global - with no regional servers, because ping does not matter, because there is no micro by design
- games that have light and spacious ui, like wikipedia webpages or a text document - with graphics being more schematic in nature, like Heroes of Might and Magic 3 for example
- jumping a little bit ahead: acutally, the game should have inlimited scenarios created by anyone and installable via a link from github, where scenario is a game of its own, with its own goals and ui
- but even without understanding multiple scenarios, having games that don't make your eyes bleed and are more board-like, rather than blinking reality replacement, would be nice
- games that have a global automated tournament system, non-moneytized, with points and events that anyone can join, with the best performers competing in weekly/monthly/seasonal finals
- it's a simple idea: open the game ui, join the event, see the brackets/groups and automated scehdule and play the tournament
- game scenarios and tournaments should be designed so that they have approximately fixed duration - by making games themseves time-framed, like real sports matches are (or turn based games are)
- so it is possible to play a tournament in the evening or play a bigger one on the weekend
- automated - because if you've been watching e-sports, you know - players (and performers in general) are most frustrated by waiting
- games that are forkable and we don't wait for companies to make behind the doors decisions and drop updates.. every 6 months or 5 years
- if game system by design allows people to create scenarios and install them via link, there will be an ocean of unique games, and the best over time will become e-sports standard
- instead of some company dropping on you what the game and the standard is
- we have game ideas, but we don't have a system to unite them into an e-sports
- DeathStarGame project will be one of such new game systems built for e-sports, with link-installable sceanrios, free, non-commercial and global

## what would the actual software sysmtem look like? like github

- at this point we know that we want a no-micro spacious think-focused, turn-based global game and e-sports
- and the ui and user experience with tournaments should be light and spacious, and the best example of such system is github.com
- in my opnion, as if now, it's the best system in terms of user experience, ui and temple-like nature and spirit of the project; github is unprecedented
- the game system that we want should be mostly github, with a mix of wikipedia and liquipedia.net - those have amazing but simple wiki pages for brackets and tournaments
- in comparison to [ESL](https://play.eslgaming.com/starcraft/global/sc2/open/1on1-series-europe/cup-1/bracket/?contestantId=4742043) official website for sc2 tourneys which an ungly abomination close to linkedin in being an antisoftware
- imagine a github-like with wiki flavour system where users are players, where events and games are sort of like repos ...
- point is: user experince-wise it's github, we open it in browser, but focused on games and tournaments

## first instinct - turn-based game like Heroes of Might and Magic

- well, HoMM3 is a great game, the best yet, in my opinion
- so what if we were to make a similar game, but in browser? and a github like system around it? would be amazing
- that was basically my initial thinking
- because HoMM3, if we make graphics even more light, editor-like, make it for the browser, has a number of attributes that we want
- if we take the idea of Hearthstone Battlegrounds of fixed game duration, and make HoMM3-like game with , for example, a game being 10mins exactly, it can be a competetive online game
- all that is configurable to achieve what we want with tournaments, and the gameplay does not require micro
- honestly, it's pretty much a great idea, and such e-sport github-like gobal system was out there, I would be playing already
- except for: control contorl control

## rant: e-sports games are about control and not about composing/buidling

- I held a guitar in my hands and even played some cords, but that's about
- but hearing music by say John Williams, who would not have this feeling of awe and a wish to be able to compose like that?
- we think so much in terms of competing for scarce resources (they are not, by the way, we make it scarce), dominating others, denying others, controlling
- just think about how many real sports and now e-sports games focus on domination, taking over, imposing your will over others? 
- yet, many are about better performance, exection
- but which are about building? composing?
- computers are a marvel: we can build systems, anything we can imagine
- and yet every game I played in childhood - Heroes of Might and Magic 3, Starcraft, Age of Empires - are about contorl and denial with little buidling and no combining/composing of elements
- but even that very fixed nature of strategy games where we build a base from small to large over time - it makes them the best yet games of the world
- but still: we think not in terms of "who can build better, solve better given the same resources, while being able to compose from elements"
- rather in terms of "deny oponent, take over, accumulate more, control more territory/units"
- have you noticed? most games have only one form of exploration - accumulation of resources, artifacts
- yet, there is little composing, building
- however, some games like Hearthstone Battlegrounds do focus on build by composing, but only within system custom game laws, created by makers of the game
- what if playing the game was more about "hey, how can I define behaviour of entities/system better to do this and that?"
- or "what can I build given these elements, which are more generically combinable"
- of course, some game scenarios should and will be about conquest and scoring better points
- but the gameplay should somehow (that's may be clear laready or we'll get it) allow for combining of smaller elements, yet it should be generic for all those user created games...
- having a mindset of: solve better, build better, define better behaviour, score more points because you composition/build performs better
- you may already understand where this is going

## considering new HoMM3-like game but about building/composing of elements and github-like system around it

- well, what is HoMM3 like and what do we take change
- HoMM3 has 7-10 castles - basically factions/civilizatoins - and different armies for every castle
- then there is a grid like map with objects on it
- and there are heroes - basically units we move on board to collect/fight
- it is essentially a modern chess
- and it has the features we want: no micro, think-focused (we'll see later we can do better), can have simulteneous turns, can be about building, not just mutual denial
- except we want to make it about building from smaller elements, allow players to compose from elements
- if adjusted and designed for e-sports in terms of making games fixed in time, it's what we want
- so let's imagine the new what? civilizations, right? and units, so come up with all those creatures and numbers (attack,speed etc.) and make it into a game, right?
- and we try
- and the thought that comes to mind is this: wait a minute, we are basically creating and arbitrary world with it's laws and all those values that is a very singual thing
- basically, it's like righting a book with characters: author(or a few athours) create the story and others should enjoy that one story
- and that works for movies and books - because there are many movies and books
- imagine that there is only one movie and we have to watch it every day.. no matter good or bad, it gets stale
- well, playing sports or e-sports is exaclty that - we take one set of rules and entities (game) and we repeat it over and over
- again, with movies - great, because many people make different movies (in reality though, we know screnplays are also copy-cat), but nevertheless, stories are limited only to our imagination
- but in e-sports - players play the same set of maps for months, doing the same build orders and focusing on execution and mechaincs
- so it's always like this: someone creates a game like Starcaft or Age of Empires or Heroes, each being a very fixed world and we spend years as part of it
- so every game is like a movie, and every e-sport is basically who mouse-clicks inside that one movie best
- and we love it.. why? because of people, communication, competition - personally I hate the game itself, but I love people around the game, talking, community
- like in real sports, it's about characters and stories around the game, stats, the game is actually secondary
- so ok, every game is a fixed never changin story, with it's unique gameplay and we can only ever realistically compete in one game.. not ideal
- and if you watch streams - really watch streams on twitch - you know, that people are mostly bored and wait for the next game to come out
- and with one-time-adventure  games or open world games - that's fine, that's by design, but *they are not e-sports*
- to be clear
    - we started thinking about our own HoMM3-like game
    - but we see the limitation - it's just one fixed world
    - and if it becomes and e-sport, it means players will have to learn that fixed world and be best at just this one world, which is so limiting by design..
    - if games are like movies, existing e-sports are designed to enclose players inside one sceanrio for years
    - and with HoMM3 like game we're gonna do the same - just make another one-fixed-world
- well, let's make a system like we want, but allow for users to create *their own games* - this way we'll have *one system, but many games*
- ok, so we'll build our own HoMM3 like game and other people will build theirs and for each tournament players will be able to select games they want to use in groups and brackets
- yeah but wait, why aren't there such torunaments already? using different existing games? well, there were such mixed events - for example, players played Starcarft and Warctaft.., but it never became a norm
- why? first, it's difficult to learn those games - each has unique gameplay
- second, it's leagcy sofware, and games belong to different companies - so each game is its own system, and lauching and swithcing is dreadful user experience
- and even with the same game like Starcarft or Warcraft - most players choose to play one civilization, because it's difficult to be good at all of them
- but it's due to clikc nature, because in Heroes of Might and Magic 3 it's a norm to compete using any castle
- big picture: we have multiple games, but no mixed tournaments - it's physically difficult to swtich between games, let alone learn each and every one
- ok, but with our github-like system and games being within the browser, can we overcome it? can we make it possible to design multiple such games and make it a norm?
- that's the key moment: what makes a game a game? a custom set of entities and values? hotkeys? laws of mouse clicking?
- suppose, users can create many HoMM3 like games or any board games and they all work within our system
- in this case, to play every such game we need to learn it, it's entities as author have designed it
- it sounds better - users creating games and being able to play multiple games within a tournament - but it's still limiting, it's still multiple games that we have to learn the rules of..
- and the gameplay: we only click, basically, and use some other custom ui to maybe input values... but it's all someones world that we must learn...
- so what is playing then? what do we do? with mouse we click, with keyboard we can input.. into what? custom forms? what is gameplay?
- how do we make the game about creating, buidling? not about learning castles in HoMM3 or how much damage a marine does in Starcraft? 
- what is building?

## what is buidling

- that's where we find ourselves - we wanted a HoMM3 like game and github-like global e-sports system
- and we find ourselves despirited by the idea of making custom worlds and rules
- because every game is limiting in terms of building, creating - we can only do what is clickable and enter values into ui
- well, but what are the exmples in reality of people creating, building and enjoying the process?
- well, writers write, painters paint, scientist develop a knowldge base across generations, buidling from lego elements..
- it's about building small elements using the laws of that world
- yes, it's bounded, but what kind of buidling and set of elements is least bounded? 
- for music it will be sounds, and ther virtually unlimited possibitiesin composing sounds, yet there are rules and principles
- same with painting: points, layers,colors - well, art in general
- but more importantly - language is
- language is what we use to accumulate and share knowledge, express ourselves (which is a composition of words)
- and it's build of elements: sounds into letters into words into senstences
- so basically speaking and writing is the game of buidling and composing from smaller elements
- and using informal language - like English - is the most unbounded form of creation possible, apart from combining it with other forms - music and pictures
- another way to think about it is via science for example: the scientist observe the material world and describe it (name particles and formulate functions/laws) in words
- overtime some words become deprecated and new terms are coined - so the science as a vocubulary and written text using it evolve (like a virtual repository of informal code)
- so it's a constant process of building by developing a language, by still using the same basic elements (letters, grammar, words) but advancing the system of knowledge  
- at some point - which is a thought taken from "A brief history of time" by Stephen Hawking - physicits may discover that particles are essentially a single particle behavig differently
- if it happens, all those words - names of particales - will become deprecated and replaced with a new name and a single fucntion, describing the behavior
- so language is the foundation of building, how can we build an e-sport around it? what would the games be?
- we just looked at imformal languages, but the next best thing is formal - programming lanugages - which are more limiting, but still virtually unbounded
- and programming is literally a process of compsing and building using a language, but it is complex; or is it?
- we all are already playing the game of building and creating every day - by speaking, writing or programming (drawing and music etc. as well, but we'll focus on the language)
- and like everyone is in awe of great musicians and composers, the same way people view programming - magical, but too complicated
- however, that complexity is mostly tooling related: it's like driving a car in the city - it's not driving that is complex, but the city
- it's not the language, but networking and develpment setup and knowing tools and environment - knowing the city
- but on the level of logic, collections, data, process loops, queues, functions - those are fun when everything works
- big picture: the planet is already playing the game of making software, it's lifelong game with a scene (github) and even competetion (better tools, system prevail)
- what is needed is to scale it down into an e-sports, where players have the language as the gameplay, a real world language
- think about this: when some people make some games and create rules for example of how elements can be combined into new elements - they've created their own small ineligant lanugage
- compare it to real programming language that generations of people put their heart and knowledge into, which one can be a foundation of a popular and long-living e-sport?
- clicking and hotkeys are also a languge, but it's limited to control only: tell units to do this, go here, with shift+click (draw a path for unit to move along) being the closest thing to programming
- ideally games in the future - it's even now possible with global deeplerning and AI like google - to use informal language of the system for players to program behaviors and entities
- but formal - programming - languages will always exisit, as they are a way for humans to express programs and *share* with others, the fact the computer can execute it is a sideeffect
- it's already happening, but we'll be more and more programming droids and other systems - should not we already have a game like this ?
- [original notes on why use language as the gameplay](https://github.com/DeathStarGame/DeathStarGameblob/c2231ab989d46aa056765d8190f0f4e0bad848c4/docs/search-for-the-game.md#building-is-about-developing-a-language)
- [game scenario ideas](https://github.com/DeathStarGame/DeathStarGameblob/c2231ab989d46aa056765d8190f0f4e0bad848c4/docs/search-for-the-game.md#examples-of-scenarios)

## tying it together: building and the game

- so we want a github-like global e-sports system
- with users being able to create games and better games becoming the standard
- we want games without micro and about buidling from elements, composing, defining behavior, not just control
- we want those games to be unique and unlimited in terms of design, yet share a single gameplay - so that players can master that gameplay and be able to play any scenario
- it is important for the success of an e-sport game for gameplay to be consistent
- imagine players coming and leaving: scnearios will change, but there should be single gameplay foundation for scenarios
- and we understand, that informal language is the most unbounded tool for buidling, with formal being second, but still unlimiting
- if we as players can think in terms of defining and buidling and use a launguage for that, that would be very inspiring
- so what all that means?
- well, users can create scnearios - each being a game of its own
- a simple scenario can be writing a function or two that defines how a rover moves on the surface of the planet
- or defining/adjusting behavior of armies and than wathcing the fight
- scenarios have fixed duration, e.g. 10mins  with simulations run every 2min
- every scenario is up to the author, but generation should be unique every time (even goals can be random) so even a single scenrio isn't stale
- but after the game starts - there should be no randomness - which is the quality of the best esport games, like chess or Starcraft
- so for two minutes players observe the map and write a little amount of code, mostly they amke decisions given the unique generaion
- the code programs or adjsuts the behavior of rover on mars so that it could visit more places
- the player that scores the most wins
- [game scenario ideas](https://github.com/DeathStarGame/DeathStarGameblob/c2231ab989d46aa056765d8190f0f4e0bad848c4/docs/search-for-the-game.md#examples-of-scenarios)
- the scenarios and duration and goals - all should be configurable, so we can have a super complex scenarios as well as simple
- an as they will be open source and user created, installable via a link, the better scenarios over time will become the standard for e-sports
- and all the progamers will have to learn.. a real programming language, as opposed to being forced into click-grinding
- people spend years inside dark repetetive games just because we guided by companies who put revenue above thinknig what is better and healthier and repsective to the idea of a human being
- e-sports is still a game, it's not creation, but it should be a miniature of it, a model, an abstraction over programming as a one of the most amazing forms of building
- so this new e-sports is a model of reality - people programming droids and systems, expressing logic - but made into an e-sport with tournaments
