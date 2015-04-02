#!/bin/sh

set -e  # fail upon any error

cd $(dirname "$0") # cd into *this* director

# loop over all the sub-directories that contain a mvnInstall.sh
# and run mvnInstall.sh in that directory
for f in ./*/mvnInstall.sh ; do
    cd $(dirname "$f")
        ./mvnInstall.sh
    cd ..
done
