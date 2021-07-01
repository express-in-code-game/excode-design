# http://www.gnu.org/software/make/manual/make.html
# https://gist.github.com/isaacs/62a2d1825d04437c6f08


repl:
	@ clj -A:repl

main:
	@ clojure

uberjar:
   clojure -X:depstar uberjar \
    :aot true \
    :jar target/deathstar.jar \
    :aliases '[:core :app]' \
    :main-class deathstar.peer.main

# DOCKER_DIND_IMAGE_NAME := "deathstar-dev-dind"

#build-dind:
#	@ echo "buidling docker iamge needed for testing"
#	@ docker build -t $(DOCKER_DIND_IMAGE_NAME) -f $(shell pwd)/test/docker/dind.Dockerfile $(shell pwd)/test/docker

# purge:
#	@- docker image rm $(DOCKER_DIND_IMAGE_NAME)
