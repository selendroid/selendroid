#!/bin/bash 
mvn install:install-file -Dfile=classes.jar \
  -DgroupId=org.apache.cordova -DartifactId=cordova \
  -Dversion=3.7.0 -Dclassifier=android -Dpackaging=jar