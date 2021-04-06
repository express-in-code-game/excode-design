#!/bin/bash

repl(){
    clj -A:core:app:repl
}

shadow(){
    ./node_modules/.bin/shadow-cljs "$@"
}

main(){
  clojure -A:core:app
}

watch(){
  npm i
  shadow -A:shadow:core:ui watch :main
}

server(){
    shadow -A:shadow:core:ui server
    # yarn server
}

compile(){
    npm i
    shadow -A:shadow:core:ui compile  :main
}

release(){
    npm i
    shadow -A:shadow:core:ui release :main
}

uberjar(){
  clojure -X:depstar uberjar \
    :aot true \
    :jar target/deathstar.jar \
    :aliases '[:core :app]' \
    :main-class deathstar.app.main
}

"$@"