#!/bin/bash

link_spaces(){

  mkdir -p spaces/ui/ spaces/ui/common

  ln -s ../../.vscode spaces/ui/.vscode
  ln -s ../../ui/src/ spaces/ui/src
  ln -s ../../ui/resources/ spaces/ui/resources
  ln -s ../../ui/shadow-cljs.edn spaces/ui/shadow-cljs.edn
  ln -s ../../../common/src spaces/ui/common/src
  ln -s ../../../common/test spaces/ui/common/test

  mkdir -p  spaces/app/  spaces/app/common

  ln -s ../../.vscode spaces/app/.vscode
  ln -s ../../app/src/ spaces/app/src
  ln -s ../../app/test/ spaces/app/test
  ln -s ../../app/resources/ spaces/app/resources
  ln -s ../../app/deps.edn spaces/app/deps.edn
  ln -s ../../../common/src spaces/app/common/src
  ln -s ../../../common/test spaces/app/common/test


}

"$@"