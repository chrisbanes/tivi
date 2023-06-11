// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)
                implementation(libs.kermit.kermit)
                implementation(libs.kotlininject.runtime)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.google.firebase.crashlytics)
                implementation(libs.timber)
            }
        }
    }
}

android {
    namespace = "app.tivi.core.logging"
}
