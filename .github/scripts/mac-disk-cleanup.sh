#!/bin/bash

echo "Disk space before cleanup..."
df -h /

echo "Removing unnecessary files to free up disk space..."
# https://github.com/actions/runner-images/issues/2840
sudo rm -rf \
  /opt/hostedtoolcache \
  /opt/google/chrome \
  /opt/microsoft/msedge \
  /opt/microsoft/powershell \
  /opt/pipx \
  /usr/lib/mono \
  /usr/local/julia* \
  /usr/local/lib/node_modules \
  /usr/local/share/chromium \
  /usr/local/share/powershell \
  /usr/share/dotnet \
  /usr/share/swift

echo "Searching for Xcode versions:"
find /Applications -name "Xcode_*" -maxdepth 1 -mindepth 1
echo "Removing old Xcode versions..."
find /Applications -name "Xcode_*" -maxdepth 1 -mindepth 1 | grep -v `cat .xcode-version` | xargs rm -rf
echo "Available Xcode versions after removal:"
find /Applications -name "Xcode_*" -maxdepth 1 -mindepth 1

echo "Disk space after cleanup..."
df -h /
