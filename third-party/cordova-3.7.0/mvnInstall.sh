#!/bin/bash
pushd `dirname $0`
mvn install:install-file -Dfile=classes.jar \
  -DgroupId=org.apache.cordova -DartifactId=cordova \
  -Dversion=3.7.0 -Dclassifier=android -Dpackaging=jar
popd
