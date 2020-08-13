#!/bin/bash

origins(){
  git remote add bb https://bitbucket.org/fo4ram/mult
  git remote add gl https://gitlab.com/fo4ram/mult.git
  git remote -v
}

dc(){

    docker-compose --compatibility \
        -f docker-compose.yml \
        "$@"
}

"$@"