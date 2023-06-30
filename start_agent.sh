#!/bin/bash
# https://github.com/appium/appium-docker-android/blob/master/Appium/start.sh
# It is workaround to access adb from androidusr
echo "Prepare adb to have access to device"
sudo /opt/android/platform-tools/adb devices >/dev/null
sudo chown -R 1300:1301 .android
echo "adb can be used now"

java -jar agent.jar