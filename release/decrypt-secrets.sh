#!/bin/bash

decrypt() {
  PASSPHRASE=$1
  INPUT=$2
  OUTPUT=$3
  gpg --quiet --batch --yes --decrypt --passphrase="$PASSPHRASE" --output $OUTPUT $INPUT
}

if [[ ! -z "$ENCRYPT_KEY" ]]; then
  # Decrypt Release key
  decrypt ${ENCRYPT_KEY} release/app-release.gpg release/app-release.jks
  # Decrypt Play Store key
  decrypt ${ENCRYPT_KEY} release/play-account.gpg release/play-account.json
  # Decrypt Google Services key
  decrypt ${ENCRYPT_KEY} release/google-services.gpg app/google-services.json
else
  echo "ENCRYPT_KEY is empty"
fi