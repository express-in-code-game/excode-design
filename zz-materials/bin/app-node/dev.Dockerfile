FROM local.ubuntu20-openjdk-clj-lein-node

RUN sudo apt update && sudo apt install -y xvfb libgtk2.0-0 libxss1 libgconf-2-4

ARG workdir

WORKDIR ${workdir}