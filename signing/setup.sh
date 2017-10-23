#!/bin/sh

ENCRYPT_KEY=$1

# Decrypt Release key
openssl aes-256-cbc -d -in signing/app-release.aes -out signing/app-release.jks -k $ENCRYPT_KEY

# Decrypt Play Store key
openssl aes-256-cbc -d -in signing/play-account.aes -out signing/play-account.p12 -k $ENCRYPT_KEY
