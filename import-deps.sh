#!/bin/bash

# This file is for internal Axway use only.
# It allows an Axway developer to copy the latest local build jar into YamlES project

local_repo=apigateway-dependencies

copyDeps() {
    local group=$1
    local artifact=$2
    local version=$3
    mvn install:install-file \
            -Dfile=${HOME}/.m2/repository/${group//./\/}/${artifact}/${version}/${artifact}-${version}.jar \
            -DgroupId=${group} \
            -DartifactId=${artifact} \
            -Dversion=${version} \
            -Dpackaging=jar \
            -DgeneratePom=true \
            -DlocalRepositoryPath=${local_repo}
}

copyDeps com.axway.apigw.es es-core 7.7.0.20200530-SNAPSHOT
copyDeps com.axway.apigw apigw-common 7.7.0.20200530-SNAPSHOT

