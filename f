#!/bin/bash

dev(){

  # lein repl :start :host 0.0.0.0 :port 7788
  # lein repl :headless :host 0.0.0.0 :port 7788
  # lein repl :connect 0.0.0.0:7878
  lein repl :headless

}

dev2(){
  lein with-profile +hidpi-ui-scale repl :headless
}

run_dev(){
  lein run dev
}

run_uberjar(){
  java -jar target/app.standalone.jar
}

uberjar(){
  lein with-profiles +prod uberjar
  # java -jar target/app.uber.jar 
}

native(){
  lein native-image
}

lein_install(){
  LEIN_VERSION=2.9.4
  LEIN_DIR=/usr/local/bin/
  curl -O https://raw.githubusercontent.com/technomancy/leiningen/${LEIN_VERSION}/bin/lein && \
    sudo mv lein ${LEIN_DIR} && \
    sudo chmod a+x ${LEIN_DIR}/lein && \
    lein version
}

"$@"