#!/bin/bash

repl(){
    clj -A:core:clj:repl
}

shadow(){
    ./node_modules/.bin/shadow-cljs "$@"
}

watch(){
  npm i
  shadow -A:dev watch :main
}

server(){
    shadow -A:dev server
    # yarn server
}

compile(){
    npm i
    shadow -A:dev compile  :main
}

release(){
    npm i
    shadow -A:dev release :main
}


"$@"