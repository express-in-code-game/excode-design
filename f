#!/bin/bash

link_spaces(){

  mkdir -p spaces/ui/ spaces/app/

  ln -s ../../ui/src/ spaces/ui/src
  ln -s ../../ui/resources/ spaces/ui/resources
  ln -s ../../ui/deps.edn spaces/ui/deps.edn

  ln -s ../../app/src/ spaces/app/src
  ln -s ../../app/resources/ spaces/app/resources
  ln -s ../../app/deps.edn spaces/app/deps.edn

}

"$@"