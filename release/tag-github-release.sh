#!/bin/bash

if [[ -z "$CIRCLE_TAG" ]]; then
  echo "Tag name is empty"
  exit 1
fi

ARTIFACTS_FOLDER="/tmp/tivi-artifacts-$CIRCLE_BUILD_NUM"

mkdir -p ${ARTIFACTS_FOLDER}
cp app/build/outputs/apk/release/app-release.apk ${ARTIFACTS_FOLDER}/tivi-release.apk
cp app/build/outputs/apk/debug/app-debug.apk ${ARTIFACTS_FOLDER}/tivi-debug.apk

ghr -t ${GITHUB_TOKEN} \
    -u ${CIRCLE_PROJECT_USERNAME} \
    -r ${CIRCLE_PROJECT_REPONAME} \
    -c ${CIRCLE_SHA1} \
    -delete \
    ${CIRCLE_TAG} \
    ${ARTIFACTS_FOLDER}
