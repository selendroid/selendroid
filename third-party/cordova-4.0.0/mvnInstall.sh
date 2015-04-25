#!/bin/bash 
mvn install:install-file -Dfile=classes.jar \
  -DgroupId=org.apache.cordova-android -DartifactId=cordova \
  -Dversion=4.0.0 -Dclassifier=android -Dpackaging=jar