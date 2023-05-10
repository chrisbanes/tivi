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
    id("app.tivi.multiplatform")
    alias(libs.plugins.cacheFixPlugin)
    id("app.tivi.android.library")
    alias(libs.plugins.moko.resources) // needs to be enabled after AGP
}

kotlin {
    jvm("desktop")
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.coroutines.core)

                api(libs.moko.resources)
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

tasks.withType(com.android.build.gradle.tasks.MergeResources::class).configureEach {
    dependsOn(tasks.getByPath("generateMRandroidMain"))
}

tasks.withType(com.android.build.gradle.tasks.MapSourceSetPathsTask::class).configureEach {
    dependsOn(tasks.getByPath("generateMRandroidMain"))
}
