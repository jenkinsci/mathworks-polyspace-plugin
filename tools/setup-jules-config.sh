#!/bin/bash

set -eux
cd /app
MAVEN_VERSION="3.9.6"
INSTALL_DIR="/app/maven"
MAVEN_HOME="$INSTALL_DIR/apache-maven-$MAVEN_VERSION"
MAVEN_BIN="$MAVEN_HOME/bin"
mkdir -p "$INSTALL_DIR"
if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven $MAVEN_VERSION"
    wget -q https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz -P /tmp
    echo "Extracting Maven to $INSTALL_DIR"
    tar -xzf /tmp/apache-maven-$MAVEN_VERSION-bin.tar.gz -C $INSTALL_DIR
else
    echo "Maven $MAVEN_VERSION is already present in $INSTALL_DIR"
fi
export PATH="$MAVEN_BIN:$PATH"
export M2_HOME="$MAVEN_HOME"
echo $PATH
echo ls $INSTALL_DIR
ls $INSTALL_DIR
echo ls $MAVEN_HOME
ls $MAVEN_HOME
which mvn
mvn -version
java -version