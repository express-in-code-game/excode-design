FROM ubuntu:20.04

ENV DEBIAN_FRONTEND="noninteractive"

## core
RUN apt-get update && \
    apt-get install -y \
            sudo  \
            git-core  \
            rlwrap  \
            software-properties-common  \
            unzip wget curl net-tools lsof \
            zlib1g-dev gcc libc6-dev \
            build-essential

RUN sudo apt update && sudo apt install -y xvfb libgtk2.0-0 libxss1 libgconf-2-4

WORKDIR /tmp

## openjdk
# RUN apt-get update && \
#     apt-get install -y openjdk-8-jdk
RUN apt-get update && \
    apt-get install -y openjdk-11-jdk
# RUN apt-get update && \
#     apt-get install -y openjdk-14-jdk

# ## graalvm
# # https://github.com/arjones/docker-graalvm/blob/master/Dockerfile
# # https://github.com/OlegIlyenko/graalvm-native-image/blob/master/Dockerfile
# ENV GRAALVM_VERSION=21.0.0.2
# ENV SUFFIX_DIR=java11-${GRAALVM_VERSION}
# ENV PATH $PATH:/usr/local/graalvm/bin
# #  dir will be graalvm-ce-java8-${GRAALVM_VERSION}
# RUN curl -Ls "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAALVM_VERSION}/graalvm-ce-java11-linux-amd64-${GRAALVM_VERSION}.tar.gz" | \
#     tar zx -C /usr/local/ && \
#     ls -l /usr/local/ && \
#     rm -f /usr/local/graalvm-ce-${SUFFIX_DIR}/src.zip && \
#     ln -s /usr/local/graalvm-ce-${SUFFIX_DIR} /usr/local/graalvm && \
#     rm -fr /var/lib/apt
# RUN gu install native-image

## clojure
ENV CLOJURE_TOOLS=linux-install-1.10.2.774.sh
RUN curl -O https://download.clojure.org/install/$CLOJURE_TOOLS && \
    chmod +x $CLOJURE_TOOLS && \
    sudo ./$CLOJURE_TOOLS && \
    clojure -Stree

## leiningen
ENV LEIN_VERSION=2.9.5
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


## golang
# RUN curl -LO https://get.golang.org/$(uname)/go_installer && chmod +x go_installer && ./go_installer && rm go_installer
ENV GOPATH=/root/go
ENV PATH=$PATH:/usr/local/go/bin:${GOPATH}/bin
ENV GO_DIST=go1.15.7.linux-amd64.tar.gz
ENV GO111MODULE=on
RUN curl -LO https://golang.org/dl/${GO_DIST} \
    && tar -C /usr/local -xzf ${GO_DIST} \
    && rm ${GO_DIST} \
    && go version


ARG workdir
WORKDIR ${workdir}