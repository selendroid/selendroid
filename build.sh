#!/bin/bash
# script to simplify build and deploy
mvn clean install
adb install -r selendroid-server/target/selendroid-server-0.4-SNAPSHOT.apk
./selendroid-server/start-server.sh 
