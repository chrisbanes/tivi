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
    alias(libs.plugins.ksp)
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)

                api(libs.tmdb.api)
                implementation(libs.ktor.client.core)

                api(libs.kotlin.coroutines.core)

                api(libs.kotlininject.runtime)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.okhttp.okhttp)
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}

dependencies {
    add("kspJvm", libs.kotlininject.compiler)
}
