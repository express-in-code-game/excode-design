## building the project for web 3.0 - decentralizaed, distributed

- sockets vs peers
    - peers, we need to start thinking in terms of peers
    - socket will be only used to connect from web ui to locally running IPFS peer node
    - it's a new way of thinknig: we don't connect socket to the hub, we are peers and the system thinks in terms of peers
- with that, how would we develop locally? well, peers seems to be perfect for that, if we run them in docker
    - because every such peer in docker container will have a separate port, like localhost:3001, so we can open a tab per player and auth (identity) would not conflict

<img  height="512px" src="./svg/2020-10-14-ipfs-peers-in-docker.svg"></img>

## system is simple: IPFS peers and decentralized distirbuted graph db  

- peers already solved by IPFS
- db
    - should be decentralized and distributed, with peers opting into how much they will store
    - should be queried as a graph (e.g. graphql), with decentralized queries
        - db maintains which peers have what (just like IPFS or using IPFS)
        - when we query on a peer, that query hops around and results are returned and aggregated and we get the result in our app
        - so programmatically, we abstractly ask "cluster of peers, what are the events?" and wait for queiry to resolve
    - we should be able to subscribe to certain queires like "current events" or "list of players in this tournament"
        - every time a trasaction to db happens, all peers get data and update their local db and push data to gui to render
- joining/leaving events/games
    - all done via db and publishing updates to peers
    - transaction -> send data to relevant peers (all or subgroup)
- playing a game
    - same - done via db
    - say, 8 players play a team game within a tournament
    - in an efficient manner (every 5-10sec for example), players actions/state are transacted into db and other 7 peers get's a query update (because each peer subscribed for a query)
    - running the simulation cycle happens the same: player's state (code) is submitted, db changes, peers get data and *every* peer runs a simulation as a sideeffect for gui
    - one of the peers simultaion result is transacted to db as the new game state, all peers get the query update
- identity
    - vid DID (decentralized identity)