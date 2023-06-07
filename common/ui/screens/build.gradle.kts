// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.kotlin.parcelize)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.circuit.runtime)
            }
        }
    }
}

android {
    namespace = "app.tivi.screens"
}
