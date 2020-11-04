FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y \
            sudo  \
            git-core  \
            rlwrap  \
            software-properties-common  \
            unzip wget curl net-tools lsof

ARG workdir

WORKDIR ${workdir}

# RUN git clone https://github.com/DeathStarGame/DeathStarGame
RUN git clone https://github.com/sergeiudris/deathstar.lab && git clone https://github.com/cljctools/cljctools
RUN mv deathstar.lab/deathstar/bin DeathStarGame && rm -rf deathstar.lab
