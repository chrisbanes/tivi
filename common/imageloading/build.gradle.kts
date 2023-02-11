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
    namespace = "app.tivi.common.imageloading"
}

dependencies {
    implementation(projects.base)
    implementation(projects.core.logging.api)
    implementation(projects.core.powercontroller.api)
    implementation(projects.data.models)
    implementation(projects.common.ui.view)
    implementation(projects.api.tmdb)

    implementation(libs.androidx.core)

    implementation(libs.kotlininject.runtime)
    ksp(libs.kotlininject.compiler)

    api(libs.coil.coil)
}
