# FROM openjdk:11.0.7-jdk@sha256:3baf00549ba72316c8cfe7a1f462598f2836f724419c2ef93adfb4ffc112479c
# FROM openjdk:14.0.2-jdk-slim@sha256:4d5d0aa4a9b902052d9a54d153db8bf0b4ac4df7cb67d929eeb3ff60467b4cdb
# FROM openjdk:14.0.2-jdk@sha256:afe7bebffdc20fb992bd67dba0a15359db806a5c3ee5bb78e88ac3c9f20c684b
FROM openjdk:14.0.2-buster@sha256:fffe674507e24eb1d0de7c1d3d0749534e9b8a655bd2629d783bad576d923d06

## clojure
ENV CLOJURE_TOOLS=linux-install-1.10.1.466.sh
RUN curl -O https://download.clojure.org/install/$CLOJURE_TOOLS && \
    chmod +x $CLOJURE_TOOLS && \
    ./$CLOJURE_TOOLS && \
    clojure -Stree

## leiningen
ENV LEIN_VERSION=2.9.3
ENV LEIN_DIR=/usr/local/bin/
RUN curl -O https://raw.githubusercontent.com/technomancy/leiningen/${LEIN_VERSION}/bin/lein && \
    mv lein ${LEIN_DIR} && \
    chmod a+x ${LEIN_DIR}/lein && \
    lein version

## node
RUN curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
    apt-get install -y nodejs 
RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg |  apt-key add - && \
    echo "deb https://dl.yarnpkg.com/debian/ stable main" |  tee /etc/apt/sources.list.d/yarn.list && \
    apt-get update && apt-get -y install yarn

## ctx
WORKDIR /ctx/app
# COPY deps.edn .
# RUN clojure -A:core:dev:optimized -Stree

# EXPOSE  2200 5101 5102