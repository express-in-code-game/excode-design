#!/bin/bash

link_spaces(){

  # SPACE=clj
  # mkdir -p  spaces/$SPACE/ spaces/$SPACE/starnet/ spaces/$SPACE/resources/public/

  # ln -s ../../.vscode spaces/$SPACE/.vscode
  # ln -s ../../../system/src/starnet/alpha spaces/$SPACE/starnet/alpha
  # ln -s ../../system/test spaces/$SPACE/test
  # ln -s ../../../../ui/resources/public/css spaces/$SPACE/resources/public/css
  # ln -s ../../apps/server/deps.edn spaces/$SPACE/deps.edn

  # SPACE=cljs
  # mkdir -p  spaces/$SPACE/ spaces/$SPACE/starnet/ spaces/$SPACE/resources/public/

  # ln -s ../../.vscode spaces/$SPACE/.vscode
  # ln -s ../../../system/src/starnet/alpha spaces/$SPACE/starnet/alpha
  # ln -s ../../system/test spaces/$SPACE/test
  # ln -s ../../../../ui/resources/public/css spaces/$SPACE/resources/public/css
  # ln -s ../../apps/ui/shadow-cljs.edn spaces/$SPACE/shadow-cljs.edn

  SPACE=system
  mkdir -p  spaces/$SPACE/ spaces/$SPACE/starnet/ spaces/$SPACE/resources/public/

  ln -s ../../.vscode spaces/$SPACE/.vscode
  ln -s ../../../system/src/starnet/alpha spaces/$SPACE/starnet/alpha
  ln -s ../../system/test spaces/$SPACE/test
  ln -s ../../../../ui/resources/public/css spaces/$SPACE/resources/public/css
  ln -s ../../apps/ui/shadow-cljs.edn spaces/$SPACE/shadow-cljs.edn
  ln -s ../../apps/server/deps.edn spaces/$SPACE/deps.edn

}

remove_volumes(){
  docker volume rm starnet.kafka
  docker volume rm starnet.crux
}

origins(){
  git remote add bb https://bitbucket.org/accompanyinggames/starnet
  git remote add gl https://gitlab.com/accompanyinggames/starnet.git
  git remote -v
}

push(){
  git push origin master
  git push bb master
  git push gl master
}

"$@"