// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.composeMultiplatform)
}

android {
    namespace = "app.tivi.episodedetails"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)
                implementation(projects.domain)
                implementation(projects.common.ui.compose)

                api(projects.common.ui.screens)
                api(projects.common.ui.circuitOverlay)
                api(libs.circuit.foundation)

                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(libs.compose.material3.windowsizeclass)
                implementation(compose.materialIconsExtended)
                implementation(compose.animation)
            }
        }
    }
}
