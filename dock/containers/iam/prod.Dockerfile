FROM ubuntu:18.04 AS builder

## core
RUN apt-get update && \
    apt-get install -y \
            sudo  \
            git-core  \
            rlwrap  \
            software-properties-common  \
            unzip wget curl net-tools lsof

WORKDIR /tmp

# openjdk
RUN apt-get update && \
    apt-get install -y openjdk-11-jdk

## clojure
ENV CLOJURE_TOOLS=linux-install-1.10.1.466.sh
RUN curl -O https://download.clojure.org/install/$CLOJURE_TOOLS && \
    chmod +x $CLOJURE_TOOLS && \
    sudo ./$CLOJURE_TOOLS && \
    clojure -Stree

## leiningen
ENV LEIN_VERSION=2.9.3
ENV LEIN_DIR=/usr/local/bin/
RUN curl -O https://raw.githubusercontent.com/technomancy/leiningen/${LEIN_VERSION}/bin/lein && \
    mv lein ${LEIN_DIR} && \
    chmod a+x ${LEIN_DIR}/lein && \
    lein version

## ctx
ARG APP_MAIN
ARG APP_LOCAL_ROOT
ENV APP_MAIN=${APP_MAIN}
ENV APP_LOCAL_ROOT=${APP_LOCAL_ROOT}

WORKDIR /ctx
COPY src system
WORKDIR /ctx/app
COPY dock/clj1/project.clj dock/clj1/f ./
RUN bash f uberjar

FROM openjdk:11.0.7-jre-slim

WORKDIR /ctx/app
# COPY --from=builder /ctx/app/target/app.uberjar.jar ./target/app.uberjar.jar
EXPOSE 7788 8080
# CMD ["java -jar target/app.uberjar.jar"]