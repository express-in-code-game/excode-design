#!/bin/bash

link_spaces(){

  SPACE=system
  mkdir -p spaces spaces/$SPACE/ spaces/$SPACE/cloud/ spaces/$SPACE/resources/public/

  ln -s ../../.vscode spaces/$SPACE/.vscode
  ln -s ../../../system/src/cloud/alpha spaces/$SPACE/cloud/alpha
  ln -s ../../system/test spaces/$SPACE/test
  ln -s ../../../../ui/resources/public/css spaces/$SPACE/resources/public/css
  ln -s ../../apps/ui/shadow-cljs.edn spaces/$SPACE/shadow-cljs.edn
  ln -s ../../apps/server/deps.edn spaces/$SPACE/deps.edn

}

remove_volumes(){
  docker volume rm cloud.kafka
  docker volume rm cloud.crux
}

add_origins(){
  git remote add source-gh https://github.com/event-games/cloud
  git remote add source-gl https://gitlab.com/event-games/cloud
  git remote add source-bb https://bitbucket.org/event-games/cloud
  git remote -v
}

push(){
  git push origin master
  git push bb master
  git push gl master
}

"$@"