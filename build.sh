#!/bin/bash

repl(){
    clj -A:repl
}


main(){
  clojure
}


uberjar(){
  clojure -X:depstar uberjar \
    :aot true \
    :jar target/DeathStarGame.jar \
    :aliases '[:core :program]' \
    :main-class DeathStarGame.main
}

"$@"