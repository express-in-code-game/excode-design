# http://www.gnu.org/software/make/manual/make.html
# https://gist.github.com/isaacs/62a2d1825d04437c6f08


DOCKER_DIND_IMAGE_NAME := "deathstar-dev-dind"

foo:
	@ echo "foo"

bar:
	@ echo "bar"

build-dind:
	@ echo "buidling docker iamge needed for testing"
	@ docker build -t $(DOCKER_DIND_IMAGE_NAME) -f $(shell pwd)/test/docker/dind.Dockerfile $(shell pwd)/test/docker

test: build-dind
	@ echo "test passes" 

clean:
	@ echo "clean"

purge:
	@- docker image rm $(DOCKER_DIND_IMAGE_NAME)
