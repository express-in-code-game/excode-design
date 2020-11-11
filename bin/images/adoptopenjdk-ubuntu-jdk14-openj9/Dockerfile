# https://hub.docker.com/layers/adoptopenjdk/openjdk14-openj9/jdk-14.0.2_12_openj9-0.21.0-ubuntu/images/sha256-cad6367b9bc0846479f6b24cd2b6711689c9b6b5a60695a1f9caedaa41641df2?context=explore
FROM adoptopenjdk/openjdk14-openj9:jdk-14.0.2_12_openj9-0.21.0-ubuntu@sha256:cad6367b9bc0846479f6b24cd2b6711689c9b6b5a60695a1f9caedaa41641df2

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