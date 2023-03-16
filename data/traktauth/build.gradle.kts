/*
 * Copyright 2023 Google LLC
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


import org.gradle.android.Versions.android

/*
 * Copyright 2023 Google LLC
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
    alias(libs.plugins.android.library)
    alias(libs.plugins.cacheFixPlugin)
    alias(libs.plugins.ksp)
}

kotlin {
    jvm()
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.base)
                api(projects.tasks.api)

                implementation(projects.core.logging)
            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.appauth)

                implementation(libs.androidx.activity.activity)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.core)

                implementation(libs.playservices.blockstore)
                implementation(libs.kotlinx.coroutines.playservices)

                implementation(libs.kotlininject.runtime)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.kotlininject.compiler)
}

android {
    namespace = "app.tivi.data.traktauth"

    defaultConfig {
        manifestPlaceholders += mapOf("appAuthRedirectScheme" to "empty")
    }
}
