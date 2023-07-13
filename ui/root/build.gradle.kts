// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
    id("app.tivi.compose")
}

android {
    namespace = "app.tivi.home"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)
                implementation(projects.core.analytics)
                implementation(projects.common.ui.compose)

                implementation(projects.domain)
                implementation(projects.data.traktauth)

                implementation(projects.common.ui.screens)
                implementation(libs.circuit.foundation)
                implementation(libs.circuit.overlay)
                implementation(projects.common.ui.circuitOverlay)

                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(compose.animation)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}
