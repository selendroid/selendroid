#!/bin/bash

mvn install:install-file -Dfile=classes.aar \
  -DgroupId=android.support.test -DartifactId=exposed-instrumentation-api \
  -Dversion=0.5 -Dpackaging=aar
