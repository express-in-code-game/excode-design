
## on being able to play events before the volunteer auto cluster (origin) exists

- auto-cluster is a higher level abstraction that is closer to an ideal tool, but not a blockng requirement
- the goal, clearly is to have events , so what's the approach ?
- per-event hosting: well, it's just LAN, but with event system included
    - an event is announced and discussed via existing public methods of communication
    - event organizer launches the system (which is open source and one command lauchnable in docker)
    - event organizer may or may not choose invitational format or open access
    - so either the ip/domain is publicly announced or is sent to the players/observers only
    - event is played and streamed, after the event orgnaizer may choose to upload the game data for public access
- identity is an issue with such approach, as system starts fresh, but it is, again, non blocking, a future goal
- ui app has an extension, through which player can access the server: connect/browse/query etc.
- system can be hosted on a laptop with ease, should be designed efficiently, don't do what client machines can
    - e.g. host should only transfer game events data, with derived state being computed on the clients

## no need for editor extensions: a directory with subdirs and files, edit with any editor

- game will use  a directory  (user's choice, can be a persistent git repo) to store code files
- game will create a uniquely named subdir for each game (even on restart)
- a player can edit files with the editor of their choice, and easily reference code from previous games
- /express-in-code-games-dir
    - /eas9d7as-unique-name-dir-for-a-game
        - file1.code
        - file2.code
        - ...
- files will be read by the game, it's up to the player to edit and save on time
- REPL server should be local, apply updates from upstream and handle expression evaluations 