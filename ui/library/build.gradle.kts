// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.android")
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.tivi.home.shows"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composecompiler.get()
    }
}

dependencies {
    implementation(projects.core.base)
    implementation(projects.domain)
    implementation(projects.common.ui.compose)
    implementation(projects.common.ui.view)

    api(projects.common.ui.screens)
    api(projects.common.ui.circuitOverlay)
    api(libs.circuit.foundation)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    implementation(libs.androidx.core)

    implementation(libs.compose.foundation.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material.material)
    implementation(libs.compose.material.iconsext)
    implementation(libs.compose.material3)
    implementation(libs.compose.animation.animation)
    implementation(libs.compose.ui.tooling)

    implementation(libs.coil.compose)

    ksp(libs.kotlininject.compiler)

    lintChecks(libs.slack.lint.compose)
}
