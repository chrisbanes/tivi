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
    kotlin("multiplatform")
    alias(libs.plugins.moko.resources)
    alias(libs.plugins.cacheFixPlugin)
    alias(libs.plugins.android.library)
}

kotlin {
    jvm()
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("dev.icerock.moko:resources:0.22.0")
//                api("dev.icerock.moko:resources-compose:0.22.0") // for compose multiplatform
            }
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "app.tivi.common.ui.resources"
}

android {
    namespace = "app.tivi.common.ui.resources"
}
