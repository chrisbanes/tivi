#!/bin/sh

test_apk() {
    gcloud firebase test android run \
	    --type instrumentation \
	    --app $1 \
	    --test $2 \
	    --device model=Nexus6P,version=27,locale=en_US,orientation=portrait \
	    --timeout 30m \
	    --results-bucket cloud-test-android-devrel-ci \
	    --no-record-video \
	    --no-performance-metrics
}

test_apk \
	"app/build/outputs/apk/debug/app-debug.apk" \
	"app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"