#!/bin/bash

ENCRYPT_KEY=$1

if [[ ! -z "$ENCRYPT_KEY" ]]; then
  # Decrypt Release key
  openssl aes-256-cbc -md sha256 -d -in release/app-release.aes -out release/app-release.jks -k ${ENCRYPT_KEY}

  # Decrypt Play Store key
  openssl aes-256-cbc -md sha256 -d -in release/play-account.aes -out release/play-account.json -k ${ENCRYPT_KEY}

  # Decrypt Google Services key
  openssl aes-256-cbc -md sha256 -d -in release/google-services.aes -out app/google-services.json -k ${ENCRYPT_KEY}
else
  echo "ENCRYPT_KEY is empty"
fi
