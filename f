#!/bin/bash

link_spaces(){
  mkdir -p spaces/sol/ui spaces/sol/app

  ln -s ui/src spaces/sol/ui/src
  ln -s ui/resources spaces/sol/ui/resources
  ln -s ui/deps.edn spaces/sol/ui/deps.edn

  ln -s app/src spaces/sol/app/src
  ln -s app/resources spaces/sol/app/resources
  ln -s app/deps.edn spaces/sol/app/deps.edn

}

"$@"