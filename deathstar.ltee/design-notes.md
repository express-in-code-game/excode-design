
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