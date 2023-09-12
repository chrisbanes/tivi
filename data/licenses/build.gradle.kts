// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
    kotlin("plugin.serialization") version "1.9.10"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.base)
                implementation(libs.kotlinx.serialization)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core)
            }
        }
    }
}

android {
    namespace = "app.tivi.data.licenses"
}
