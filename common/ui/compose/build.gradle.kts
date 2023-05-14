/*
 * Copyright 2020 Google LLC
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
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "app.tivi.common.compose"

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composecompiler.get()
    }

    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    api(projects.data.models)
    api(projects.core.preferences)
    api(projects.common.imageloading)

    api(projects.common.ui.resources)
    api(projects.common.ui.resourcesCompose)
    api(projects.common.ui.view)

    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    api(platform(libs.compose.bom))
    implementation(libs.compose.ui.ui)
    implementation(libs.compose.ui.uitextfonts)
    implementation(libs.compose.foundation.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material.material)
    implementation(libs.compose.material.iconsext)
    implementation(libs.compose.material3)
    implementation(libs.compose.animation.animation)
    implementation(libs.compose.ui.tooling)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    implementation(libs.coil.compose)

    lintChecks(libs.slack.lint.compose)
}
