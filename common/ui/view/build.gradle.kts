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
}

android {
    namespace = "app.tivi.common.ui"
}

dependencies {
    api(projects.data.models)
    api(projects.common.ui.resources)
    api(projects.core.logging)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.kotlin.coroutines.core)

    implementation(libs.androidx.activity.activity)
    implementation(libs.androidx.core)
    implementation(libs.androidx.emoji)
}
