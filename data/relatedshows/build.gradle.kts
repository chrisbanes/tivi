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


import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

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
    id("kotlin")
    alias(libs.plugins.android.lint)
    alias(libs.plugins.kotlin.kapt)
}

dependencies {
    api(projects.data.models)
    implementation(projects.data.db)
    implementation(projects.data.legacy) // remove this eventually

    implementation(projects.api.trakt)
    implementation(projects.api.tmdb)
    implementation(libs.retrofit.retrofit)

    api(libs.store)
    implementation(libs.kotlinx.atomicfu)

    implementation(libs.hilt.core)
    kapt(libs.hilt.compiler)
}
