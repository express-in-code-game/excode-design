FROM ubuntu:18.04

## core
RUN apt-get update && \
    apt-get install -y \
            sudo  \
            git-core  \
            rlwrap  \
            software-properties-common  \
            unzip wget curl net-tools lsof build-essential

WORKDIR /tmp

## node
RUN curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
    apt-get install -y nodejs 
RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg |  apt-key add - && \
    echo "deb https://dl.yarnpkg.com/debian/ stable main" |  tee /etc/apt/sources.list.d/yarn.list && \
    apt-get update && apt-get -y install yarn

# ARG workdir

# WORKDIR ${workdir}

RUN git clone https://github.com/microsoft/vscode
RUN cd vscode && yarn install
RUN  yarn run compile

CMD ["yarn", "web", "--no-launch", "--host", "0.0.0.0", "--port", "8080"]