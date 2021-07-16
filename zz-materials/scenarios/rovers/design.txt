rovers on Mars

## scenario program and player program

- goal of the rover is to score highest by visiting most points
- rover has a radius of vision, where it sees points and chooses which one to move towards
- when sceanrio program runs the simulation process, on every loop it sends data - rover's state and landscape (points, fields, traps, energy recharging etc. - those can be points themselves)
- player program has an operation, that receives reovers state and ladscape within vision state
- player program makes decisions and answers with the next tile (position on the map) that rover chooses to go
- scenario program gets the response (next choses position) and computes next state of the game nad rover
  - rover may have chosen the tile which drains energy
  - or scanned instead of moving, and also spent energy, reducing it's range
  - or recharged energy at a certain point
  - new state of the rover has new energy level and range
- in the next loop, sceanrio program asks for the next move again
- and rover moves in this manner until it has no energy/range
- the score is how many points it visited (but we also see other stats - distance convered, enrgy spent/rechagred
- now, we players see the whole map from the start
- and although we cannot use that data when scenario program asks for next move, it's still possible to program rover to move in the general direction we see visually on the map, but rover does not

## simulation cycle: write program - rover takes 10 steps - adjust program - next 10 steps

- we write a program (logic to make decison about next position) for 1min
- then scenario runs simulation for 10 steps/ticks - we see it on the map
- we have another minute to adjust rover's program for the next part of the journey
- write program - rover takes 10 steps - adjust program - next 10 steps - ...

## distance and energy instead of paths

- rovers sees locations and visits them
- energy spent is proportional to distance (and also field interference - so line and circle intersection)
- visting locations changes rover's state

## multiple rovers: solution to the problem of 'why not just click-draw a path for the rover'

- if we have a single rover, and it  needs to find it's path, we instinctively (and correctly) want to specify the path for the rover
- if so, it seems pointless to program rover to choose the path when we can (and in reality we do) draw a path
- instead, the sceanrio mission will be 
  - we as mission team aquired a few alien origin droids (rovers)
  - they are generically programmed and we can change that program for all of them (data over radio waves is received by every drone, we cannot pick one, time is limited we act now)
  - we sent rovers to Mars for exploration/finding something (plot thickens)
    - either reach a communication tower (or all of them)
    - or visit max number of locations to collect important data for the mission
  - we have multipple rovers, each starting in a different location on the map
  - we have limited time and we must modify their program so all of them visited maximum number of locations (or found that something, plot thickens)
- this way we don't look at a rover, but rather think "what program would be most optimal for the current scenario goal given all 4 rovers' positions"

## multiple rovers: goals and strategies

- objectives
  - reach one location with any of the rovers (find that something)
  - reach multiple specific locations locations
  - reach max number of locations
- strategies
  - prioritize one rover, make program specific to that rover's location
    - if scenario's generated objective is to react , player may choose to make program work best for that rover (which we see on the map) and let others fails
  - make program generic, giving a couple of rovers a chance to reach location(s)

## player sends a list of 10 high level operations

- each op is an option map to a function
- scenario does not send data, player defined ops are from looking at the map
- somehow think about using logic or predicates as part of data
- once scenario receives data, it advances rover ten steps, players have another minute to think/adjust their op list

## snapshot: using reagent's run-in-reaction for state

- https://github.com/DeathStarGame/DeathStarGametree/e50a0b54ace8669907429d005571e5b4cbfa766b/scenarios/rovers
- the problem is this
  - using derived state is not explcit and scattered,  given that we have processes,operaiotns and asynchrony
  - op ::move-rover arrives to main.cljc and then we jump to core, which for no reason is state aware
  - state is using reagent ratoms, which should be only in render
  - if we use process in core.cljc, it's a cycle, a deadend: we have sceanrio progam process already, it's in main
  - so we need that process to be readable and explicit: tell us what happens per operation
  - instead of complex system of deps, we need to have all that happens per operation be right near that op in the main
  - most simple thing: core becomes a namespace that is only data aware
  - functions take state (as data, not atom) and return some data, that we correclty swap into state in the main process
  - so main is the program, core is spec and resuable functions (which to reuse? when a fucntion is needed second time, it becomes a var in core, not before)
  - this way, every op in the main will have the same expressions , but this is by design: we want to explicitely read in the main program process (where we have full access to asynchrony) what happens per operation
  - it's not duplication, it's programming: we use conside library fucntions in multiple operations; if some logic is similar - into a function or change the logic, maybe it should not be
- on reactivity
  - when we use reagent ratoms and run-in-reaction, it's like deps: we specify a fn to run when state changes
  - when state changes? always on operration (event)
  - how would we "react" to operaions? we have processes for that, and we could have taps etc.
  - so instead of creating obscure web of deps (it's not readable and has no asychrony, it's outside the program), some process should take/tap to those events and perform logic (maybe certain logic for a set of ops, not just one)
  - this way, we expclitely know: channels, opetaions, processes
  - but in case of scenario - we have that process already, by design it's our program, our main process
  - so we "react" to operations and change state; how to recompute ceratin things on multiple operations? as we just went through a few lines above - put those computations into a lib (core.cljc) and invoke functions and call swap with their result in every op; this way, it's readble, we for sure know what happens in the program when ::move-rover operaion is performed