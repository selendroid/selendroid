#!/bin/bash

sudo dpkg --add-architecture i386
sudo apt update
sudo apt install -y libc6:i386 libncurses5:i386 libstdc++6:i386
sudo apt remove -y gradle
sudo mkdir /opt/gradle
wget https://downloads.gradle.org/distributions/gradle-4.0.2-bin.zip
sudo unzip -o -d /opt/gradle/ gradle-4.0.2-bin.zip
rm gradle-4.0.2-bin.zip

export ANDROID_HOME=/home/moritz/Android/Sdk/
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
export PATH=$PATH:/opt/gradle/gradle-4.0.2/bin
export MAVEN_OPTS="-Xss4m"

cd third-party/
./mvnInstall.sh
cd ..
mvn clean install
