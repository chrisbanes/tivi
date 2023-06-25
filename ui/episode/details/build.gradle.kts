// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.android.compose")
    id("app.tivi.kotlin.android")
}

android {
    namespace = "app.tivi.episodedetails"
}

dependencies {
    implementation(projects.core.base)
    implementation(projects.domain)
    implementation(projects.common.ui.compose)

    api(projects.common.ui.screens)
    api(projects.common.ui.circuitOverlay)
    api(libs.circuit.foundation)

    implementation(libs.compose.foundation.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material.material)
    implementation(libs.compose.material3.material3)
    implementation(libs.compose.material3.windowsizeclass)
    implementation(libs.compose.material.iconsext)
    implementation(libs.compose.animation.animation)
    implementation(libs.compose.ui.tooling)

    implementation(libs.coil.compose)
}
