// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.android")
}

android {
    namespace = "app.tivi.ui.overlays"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composecompiler.get()
    }
}

dependencies {
    implementation(projects.common.ui.screens)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.animation.animation)
    implementation(libs.compose.ui.tooling)
    implementation(libs.androidx.activity.compose)

    api(libs.circuit.foundation)
    api(libs.circuit.overlay)
}
