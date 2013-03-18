#!/bin/bash
mvn clean install
adb install -r selendroid-server/target/selendroid-server-0.3.apk
./selendroid-server/start-server.sh 
