ARG BASE_IMAGE
FROM ${BASE_IMAGE}
## clojure (needed for lein-tools-deps)
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

ONBUILD ARG dir_runner_from
ONBUILD ARG dir_runner_to
ONBUILD ARG dir_src_from
ONBUILD ARG dir_src_to
ONBUILD ARG cmd_build

ONBUILD COPY ${dir_runner_from} ${dir_runner_to}
ONBUILD COPY ${dir_src_from} ${dir_src_to}
ONBUILD WORKDIR ${dir_runner_to}
ONBUILD RUN ${cmd_build}
