
This document is a linear continuation of:

- [../cloud-native-system/design.md](../cloud-native-system/design.md)
- [../search-for-the-game.md](../search-for-the-game.md)
- [../origin-cluster/origin-cluster.md](../origin-cluster/origin-cluster.md)
- [./as-vscode-extension.md](./as-vscode-extension.md)

## switch account feature like in youtube

- to be able to open multiple tabs and easily select identity


## explore the idea of building without sockets for simplicity

- submit user code and get updates via http requests
- if that is limiting, definitely go for socket or sse

## thinking how to use graphql, async ops with status, app logic

<img height="512px" src="./svg/2020-10-16-graphql.svg"></img>

## explore existing self-hostable systems with sane design, re-purpose for the game

- consider systems out there that have all the intangibles - graph data layer, identity, ops, logic in a decoupled apps,reverse-proxy, simplicity...
- fork, replace apps and logic with game app(s) and processes, replace gui etc.

## app should be an extension of the system

- system should be runnable on it's own, with identity, reverse proxy, database
- apps and their ui should (even dynamically) come into the system and access data layer etc.
- apps and ui are logic, while identity, netwroking, data layer are part of the system


## apps are esentailly processes (intances), so they never talk via http, only via queue/peer abstactions

- if app needs to perform operation on another app, it does so by abstract request/push to *central* thingy: "hey, system, so this", not to each other

## identity: provider auth contradicts the design of deathstar.ltee, so identity is always self

- providers require to register apps, which would make the system not actually self-hosted

## indentity: possible to implement auth using DID (decentralized identifiers) as they seem to be the next gen and global

- is ti correct to assume,that with DID a user can login into different servers (with fresh dbs) and still maintain identity? because it's a private key file?
- can(are there tools/info) a browser UI use DIDs to auth into the system?
- where will users store DID? on some IPFS provider? "login with IPFS" ? or in browser storage ? on disk?