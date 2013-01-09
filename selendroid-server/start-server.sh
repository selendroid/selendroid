#!/bin/bash
echo 'Starting Selendroid-server'
adb shell am instrument -e main_activity 'org.openqa.selendroid.testapp.HomeScreenActivity' org.openqa.selendroid/org.openqa.selendroid.ServerInstrumentation

echo 'Activating port forwarding to port 8080'
# activate port forwarding (local port : device port )
 adb forward tcp:8080 tcp:8080
