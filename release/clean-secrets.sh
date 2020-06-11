#!/bin/sh

# Delete Release key
rm -f release/app-release.jks

# Delete Play Store key
rm -f release/play-account.json

rm -f app/google-services.json
