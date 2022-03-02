#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
export PATH="${JAVA_HOME}/bin:${PATH}"

cd /etc/app/be-users
java -jar -Dserver.port=80 \
       -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 \
       /etc/app/be-users/be-users-1.0-SNAPSHOT.jar