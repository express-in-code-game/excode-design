#!/bin/bash

repl(){
    clj -A:core:app:repl
}


main(){
  clojure -A:core:app
}


uberjar(){
  clojure -X:depstar uberjar \
    :aot true \
    :jar target/deathstar.jar \
    :aliases '[:core :app]' \
    :main-class deathstar.peer.main
}

"$@"