#!/bin/bash 

mvn install:install-file -Dfile=classes.jar \
  -DgroupId=com.google.android -DartifactId=support-v4 \
  -Dversion=21.0.3 -Dpackaging=jar