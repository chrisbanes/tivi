// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.android.compose")
    id("app.tivi.kotlin.android")
}

android {
    namespace = "app.tivi.home.shows"
}

dependencies {
    implementation(projects.core.base)
    implementation(projects.domain)
    implementation(projects.common.ui.compose)

    api(projects.common.ui.screens)
    api(projects.common.ui.circuitOverlay)
    api(libs.circuit.foundation)

    implementation(libs.paging.compose)

    implementation(libs.androidx.core)

    implementation(libs.compose.foundation.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material.material)
    implementation(libs.compose.material.iconsext)
    implementation(libs.compose.material3.material3)
    implementation(libs.compose.animation.animation)
    implementation(libs.compose.ui.tooling)

    implementation(libs.coil.compose)
}
