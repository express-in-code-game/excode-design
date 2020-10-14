## we need to think/create/build the project features - events,scenarios

- the thinking/building torunaments and events should be unlocked
- we can either design graphically/express in words all waht system does - purely in docs
- but this is not enjoybale to create sceanrios this way, it's should be done programmatically
- we want to build the project for web 3.0 - decentralizaed, distributed, with peers joining/leaving
- what is needed: design and build system in layers, so we can build it and use it, regardless of whether or not tools are ready
- so ui layer, requests, queries, identity, multiplayer ... - all that should be built and usabe, whether or not distributed and decentralized out of the gate
- however
    - we cannot take a random db and than substitute it for web 3.0 (IPFS)
    - unless the db supports this kind of storage swapping
    - the data layer of the system - querying and data schema and transport and encoding - these are the system, they cannot be "substituted later"
    - we can swap storage layer (like datomic does, dgraph or any other db - swap the store), but we cannot swap our data design
- it super simple: we need to build all layers of the system, as they should be with data abstarcation (database simply) in place, and be sure it will work with IPFS and peers
    - there is ipfs-cluster and orbitdb, what else ? ... 
- sockets vs peers
    - peers, we need to start thinking in terms of peers
    - socket will be only used to connect from web ui to locally running IPFS peer node
    - it's a new way of thinknig: we don't connect socket to the hub, we are peers and the system thinks in terms of peers
- with that, how would we develop locally? well, peers seems to be perfect for that, if we run them in docker
    - because every such peer in docker container will have a separate port, like localhost:3001, so we can open a tab per player and auth (identity) would not conflict

<img  height="512px" src="./svg/2020-10-14-ipfs-peers-in-docker.svg"></img>

- bottom line: we need to either draw/describe the whole system without code, or build it properly, with idenetity and multiplayer, but via layers
- tournamets, events, scenarios, user and player experience - those should be a daily focus, not tools/lack of abstactions/tools 