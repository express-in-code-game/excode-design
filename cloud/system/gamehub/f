#!/bin/bash

dev(){

  # lein repl :start :host 0.0.0.0 :port 7788
  # lein repl :headless :host 0.0.0.0 :port 7788
  # lein repl :connect 0.0.0.0:7878
  lein repl :headless

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

"$@"