#!/usr/bin/env bash

version_file="/config/version.txt"

__LAST_BUILD_NUM=1900

if [ -f "${version_file}" ];then
  echo "Reading existing version from ${version_file}"
  source "${version_file}"
fi

BUILD_NUM=$((__LAST_BUILD_NUM+1))

echo
echo "       Last build number: ${__LAST_BUILD_NUM}"
echo "Updating to build number: ${BUILD_NUM}"

echo "__LAST_BUILD_NUM=${BUILD_NUM}" > "${version_file}"

OUTPUT_FILE=build_environment.sh
echo "export BUILD_NUM=${BUILD_NUM}" > ${OUTPUT_FILE}

echo "Wrote output file: ${OUTPUT_FILE}"
