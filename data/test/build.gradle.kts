/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.cacheFixPlugin)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.tivi.data.test"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true

            all {
                it.minHeapSize = "64m"
                it.maxHeapSize = "128m"
            }
        }
    }
}

dependencies {
    implementation(projects.core.analytics.android)
    implementation(projects.core.logging.android)

    implementation(projects.data.followedshows)
    implementation(projects.data.episodes)
    implementation(projects.data.showimages)
    implementation(projects.data.shows)

    implementation(libs.kotlininject.runtime)

    testImplementation(projects.data.legacy)
    testImplementation(projects.data.dbRoom)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockK)

    testImplementation(libs.androidx.archCoreTesting)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.coroutines.test)

    kspTest(libs.kotlininject.compiler)

    // Needed for Tzdb
    testImplementation("org.threeten:threetenbp:${libs.versions.threetenbp.get()}")
    // Needed for Main dispatcher to work
    testImplementation(libs.kotlin.coroutines.android)
}
