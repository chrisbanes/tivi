#!/bin/bash

# Copyright 2021 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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