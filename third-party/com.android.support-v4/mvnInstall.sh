#!/bin/bash 
mvn install:install-file -Dfile=classes.jar \
  -DgroupId=android.support-v4 -DartifactId=support-v4 \
  -Dversion=r8 -Dclassifier=android -Dpackaging=jar	