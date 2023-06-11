// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.android")
    alias(libs.plugins.composeMultiplatform)
}

android {
    namespace = "app.tivi.account"
}

dependencies {
    implementation(projects.core.base)
    implementation(projects.domain)
    implementation(projects.common.ui.compose)
    implementation(projects.data.traktauth) // This should really be used through an interactor

    api(projects.common.ui.screens)
    api(projects.common.ui.circuitOverlay) // Only for LocalNavigator
    api(libs.circuit.foundation)

    // For registerForActivityResult
    implementation(libs.androidx.activity.compose)

    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.material3)
    implementation(compose.animation)
    implementation(compose.uiTooling)
}
