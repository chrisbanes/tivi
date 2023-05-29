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
