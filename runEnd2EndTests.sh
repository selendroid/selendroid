#!/bin/bash
# Copyright 2014 eBay Software Foundation and selendroid committers.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
# in compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied. See the License for the specific language governing permissions and limitations under
# the License.

function wait_for_boot_complete {
  local boot_property=$1
  local boot_property_test=$2
  echo "[emulator-$emulator_port] waiting to boot..."
  result=`adb -s emulator-$emulator_port shell getprop $boot_property 2>/dev/null | grep $boot_property_test`
  while [ -z $result ]; do
    sleep 1
    result=`adb -s emulator-$emulator_port shell getprop $boot_property 2>/dev/null | grep $boot_property_test`
  done
  echo "[emulator-$emulator_port] booted"
}


platform_version=$1
base_package="io.selendroid.testapp"
if [ -z "$platform_version" ]; then
  platform_version=18
fi

echo "Using platform version: ${platform_version}"

avd_name="debug_selendroid_"${platform_version}
selendroid_version=`grep '<version' pom.xml | cut -f2 -d">"|cut -f1 -d"<"|head -1`

echo "deleting existing emulator"
android delete avd -n ${avd_name}
echo "create a new one"
echo no | android create avd -n ${avd_name} -t android-${platform_version} --force --abi x86


START=$(date +%s)
emulator_port=5560

echo "Using: ${locale} to start an emulator for API version: ${platform_version}"
emulator -avd $avd_name -no-audio -no-boot-anim -port $emulator_port&
wait_for_boot_complete "init.svc.bootanim" "stopped"
adb -s emulator-${emulator_port} shell input keyevent 4
adb -s emulator-${emulator_port} shell input keyevent 82

END=$(date +%s)
DIFF=$(( $END - $START ))
echo "It took $DIFF seconds"

echo "Make the sdcard writable"
adb shell <<DONE
su
mount -o rw,remount rootfs /
chmod 777 /mnt/sdcard
exit
exit
DONE

echo "Install test-app apk"
adb install -r selendroid-test-app/target/selendroid-test-app-${selendroid_version}.apk

echo "Running End-to-End Tests"
mvn install -pl selendroid-test-app -DskipTests=false
# for running aspecific test
# mvn install -pl selendroid-test-app -DskipTests=false -Dtest=WaitForProgressBarGoneAwayTest

echo "Stopping Emulator"
adb -s  emulator-$emulator_port emu kill
