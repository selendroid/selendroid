#!/bin/env bash

mvn install:install-file -Dfile=classes.jar \
  -DgroupId=android.support.test -DartifactId=runner \
  -Dversion=0.5 -Dpackaging=jar
