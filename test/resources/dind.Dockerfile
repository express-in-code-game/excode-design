# docker run -it --rm \
#   -e DOCKER_TLS_CERTDIR=/certs \
#   -v /var/run/docker.sock:/var/run/docker.sock \
#   docker:latest sh


# docker run -it --privileged --rm \
#   -v /var/run/docker.sock:/var/run/docker.sock \
#   docker:dind sh

FROM docker:20.10.5

RUN apk update && \
    apk add --no-cache \
    curl git bash

## java
RUN apk add openjdk11

## clojure
ENV CLOJURE_TOOLS=linux-install-1.10.2.774.sh
RUN curl -O https://download.clojure.org/install/$CLOJURE_TOOLS && \
    chmod +x $CLOJURE_TOOLS && \
    ./$CLOJURE_TOOLS && \
    clojure -Stree

RUN apk --no-cache add rlwrap --repository http://dl-3.alpinelinux.org/alpine/edge/testing/ --allow-untrusted

## leiningen
ENV LEIN_VERSION=2.9.5
ENV LEIN_DIR=/usr/local/bin/
RUN curl -O https://raw.githubusercontent.com/technomancy/leiningen/${LEIN_VERSION}/bin/lein && \
    mv lein ${LEIN_DIR} && \
    chmod a+x ${LEIN_DIR}/lein && \
    lein version

ARG workdir="/"

WORKDIR ${workdir}


# docker run -it --rm \
#   -e DOCKER_TLS_CERTDIR=/certs \
#   -v /var/run/docker.sock:/var/run/docker.sock \
#   dev-dind:latest sh