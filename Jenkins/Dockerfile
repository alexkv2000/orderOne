FROM jenkins/jenkins:lts

USER root

# Установка Maven
RUN apt-get update && apt-get install -y maven

# Установка Docker CLI
RUN apt-get update && \
    apt-get install -y ca-certificates curl && \
    install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc && \
    chmod a+r /etc/apt/keyrings/docker.asc && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" > /etc/apt/sources.list.d/docker.list && \
    apt-get update && \
    apt-get install -y docker-ce-cli

# Установка CodeQL (опционально)
RUN apt-get install -y wget unzip && \
    cd /opt && \
    wget https://github.com/github/codeql-cli-binaries/releases/download/v2.20.4/codeql-linux64.zip && \
    unzip codeql-linux64.zip && \
    ln -s /opt/codeql/codeql /usr/local/bin/codeql && \
    rm codeql-linux64.zip

USER jenkins