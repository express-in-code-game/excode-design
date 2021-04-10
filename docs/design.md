## peer to peer

- user computers (laptops) run the game
- users can turn game on/off, yet it's a global app
- the users currently online are the network
- when node goes online, it fetches updates into local global db
- when 1000 people participate in event, their machines will host games
- like git and forks: if 8 peers are playing and host goes down, games continues
- game is cold and efficient: the global app is formed by laptops running it only, that constantly go on and off
- core mechanism - pubsub (gossip etc.) for network dataflow

## installation

- uberjar + requires docker
- but first, for developing and building decentralized networks: go full docker (uberjar is for user convenience)

## evaluation

- sci with core.async
- on jvm of the host

## scenario installation

- files downloaded with deps.cli
- required from within sci runtime, no ns collisions

## ui

- browser

## links

- open game in a browser tab via a link
- can browse history and stats - all like web app, with links that can be shared

## identity

- if we don't solve identity from start, someone will make Voobly's and run cloud servers - and we'll get not a global decentralized peer game, but services that charge people to play; the idea of the game will be trashed; unacceptable
- global decentralized identity from start

## use go-ipfs node and http api to access pubsub

- since there is no way to run existing libp2p on jvm (only go-libp2p really counts) and using nodejs is not on the trajectory, use go-ipfs node + http
- plus: game literally runs of ipfs network
- minus: need another container (not really a minus)
- plus: networking is literally decoupled from app via http
- plus: can connect to existing ipfs node (e.g. user already runs ipfs-desktop)

## tournaments

- create an event (match, tournament)
- create tennis/starcraft -like tour - series of tournaments, points with a finale 
