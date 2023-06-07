// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.base)
                implementation(projects.core.logging)
            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.appauth)

                implementation(libs.androidx.activity.activity)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.core)

                implementation(libs.playservices.blockstore)
                implementation(libs.kotlinx.coroutines.playservices)

                implementation(libs.kotlininject.runtime)
            }
        }
    }
}

android {
    namespace = "app.tivi.data.traktauth"

    defaultConfig {
        manifestPlaceholders += mapOf("appAuthRedirectScheme" to "empty")
    }
}
