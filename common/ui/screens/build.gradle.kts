// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.kotlin.multiplatform")
    id("app.tivi.android.library")
    alias(libs.plugins.kotlin.parcelize)
}

kotlin {
    jvm()
    android()

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
