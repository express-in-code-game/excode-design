
This document is a linear continuation of:

- [../cloud-native-system/design.md](../cloud-native-system/design.md)
- [../search-for-the-game.md](../search-for-the-game.md)
- [../origin-cluster/origin-cluster.md](../origin-cluster/origin-cluster.md)
- [./as-vscode-extension.md](./as-vscode-extension.md)

## what the system will lool like

- the system will run in docker, with a browser ui
- no wheel reinvention whenever possible (should be most cases)
- ui is standard, mostly mutiplayer stuff, where users can create/join/observe games
- part of the screen will be for editor (editing clojure code), part will show scenario gui
- system (server) will handle game data, hisotry and user's game files (code), preferably in graph manner to be queried, but files maybe git or some other standard way to do it
- games are processes that will run on server (and scenario simulations): when needed, user's code from gui is sent to server, game simulation is run, new state is broadcasted to everyone
- identity: done as a layer, should be standard
- in general: the system should be done applying best practices, standards and protocols
- it is a standard system operating on user data, the special part is submitting code and running game sceanrios
- lets play a game with friends/enemies
  - we install docker and run a single command that downloads docker images of the game and starts a docker deployment
  - we open a browser on localhost:port and see game ui
  - if it's the first time, we create admin account, otherwise we just login (data is persisted in a docker volume)
  - optionally we install additional scenarios via gui: go to github, find repos, copy-paste links into game's gui
  - we can play a couple of games by ourselves just to get to know scenarios
  - we invite others to our server: we open a port on our router and send our internet ip and port to others
  - they simply open their browser, type in ip:port, create their accout if the first time, and are redy to play
  - once everyone is on the server, we create an event(tournament), invite, configure (groups, brackets) and start the game
  - everyone sees an editor and scenrio's ui (a game map, scenario is a gmae of its own), periodically code is submitted and game state advances
  - the game duration is fixed (say, 10 or 15 mins, configurable), so nobody waits for nobody
  - one the game is over, scenario shows who one (most points or conquest or whatever)
  - those who lose, can observe remaining games
  - the tourney continues, someone wins and earns their bragging rights, can talk trash until the next tourney
- lets go

## switch account feature like in youtube

- to be able to open multiple tabs and easily select identity