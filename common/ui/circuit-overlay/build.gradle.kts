// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.android.compose")
    id("app.tivi.kotlin.android")
}

android {
    namespace = "app.tivi.ui.overlays"
}

dependencies {
    implementation(projects.common.ui.compose)
    implementation(projects.common.ui.screens)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3.material3)
    implementation(libs.compose.animation.animation)
    implementation(libs.compose.ui.tooling)
    implementation(libs.androidx.activity.compose)

    api(libs.circuit.foundation)
    api(libs.circuit.overlay)
}
