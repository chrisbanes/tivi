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

encrypt() {
  PASSPHRASE=$1
  INPUT=$2
  OUTPUT=$3
  gpg --batch --yes --passphrase="$PASSPHRASE" --cipher-algo AES256 --symmetric --output $OUTPUT $INPUT
}

if [[ ! -z "$ENCRYPT_KEY" ]]; then
  # Encrypt Release key
  encrypt ${ENCRYPT_KEY} release/app-release.jks release/app-release.gpg 
  # Encrypt Play Store key
  encrypt ${ENCRYPT_KEY} release/play-account.json release/play-account.gpg
  # Encrypt Google Services key
  encrypt ${ENCRYPT_KEY} app/google-services.json release/google-services.gpg
else
  echo "ENCRYPT_KEY is empty"
fi