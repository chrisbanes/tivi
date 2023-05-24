/*
 * Copyright 2018 Google LLC
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
    id("app.tivi.android.library")
    id("app.tivi.kotlin.android")
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.tivi.settings"
}

dependencies {
    implementation(projects.core.base)
    implementation(projects.common.ui.resources)
    implementation(projects.common.ui.view)
    implementation(projects.core.powercontroller)
    implementation(projects.core.preferences)

    implementation(libs.androidx.activity.activity)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    ksp(libs.kotlininject.compiler)
}
