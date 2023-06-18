// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.android")
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

    api(projects.common.ui.screens)
    api(libs.circuit.foundation)

    api(projects.common.ui.resources)
    api(projects.common.ui.resourcesCompose)

    implementation(libs.androidx.core)

    api(platform(libs.compose.bom))
    implementation(libs.compose.ui.ui)
    implementation(libs.compose.foundation.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material.material)
    implementation(libs.compose.material.iconsext)
    api(libs.compose.material3.material3)
    api(libs.compose.material3.windowsizeclass)
    implementation(libs.compose.animation.animation)
    implementation(libs.compose.ui.tooling)

    implementation(libs.paging.compose)

    implementation(libs.coil.compose)

    lintChecks(libs.slack.lint.compose)
}
