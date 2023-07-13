// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
    id("app.tivi.compose")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }

        val jvmCommon by creating
        val androidMain by getting {
            dependsOn(jvmCommon)
        }
        val jvmMain by getting {
            dependsOn(jvmCommon)
        }
    }
}

android {
    namespace = "com.vanpra.composematerialdialogs.datetime"
}
