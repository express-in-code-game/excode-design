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
    :jar target/deathstar.jar \
    :aliases '[:core :program]' \
    :main-class deathstar.peer.main
}

"$@"