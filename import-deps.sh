#!/bin/bash

# This file is for internal Axway use only.
# It allows an Axway developer to copy the latest local build jar into YamlES project

copyDeps() {
    group=$1
    artifact=$2
    jar_path=${group}/${artifact}
    mkdir -p apigateway-dependencies/${jar_path}
    cp -R ${HOME}/.m2/repository/${jar_path}/**/*.jar apigateway-dependencies/${jar_path}/
}

copyDeps com/axway/apigw/es es-core
copyDeps com/axway/apigw apigw-common

