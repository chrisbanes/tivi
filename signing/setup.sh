#!/bin/sh

ENCRYPT_KEY=$1

# Decrypt Release key
openssl aes-256-cbc -md sha256 -d -in signing/app-release.aes -out signing/app-release.jks -k $ENCRYPT_KEY

# Decrypt Play Store key
openssl aes-256-cbc -md sha256 -d -in signing/play-account.aes -out signing/play-account.json -k $ENCRYPT_KEY

# Decrypt Google Services key
openssl aes-256-cbc -md sha256 -d -in signing/google-services.aes -out app/google-services.json -k $ENCRYPT_KEY
