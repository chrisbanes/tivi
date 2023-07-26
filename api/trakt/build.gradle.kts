// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.buildConfig)
}

buildConfig {
    packageName("app.tivi.trakt")

    buildConfigField(
        type = "String",
        name = "TRAKT_DEBUG_CLIENT_SECRET",
        value = "\"${properties["TIVI_DEBUG_TRAKT_CLIENT_SECRET"]?.toString() ?: ""}\"",
    )
    buildConfigField(
        type = "String",
        name = "TRAKT_DEBUG_CLIENT_ID",
        value = "\"${properties["TIVI_DEBUG_TRAKT_CLIENT_ID"]?.toString() ?: ""}\"",
    )
    buildConfigField(
        type = "String",
        name = "TRAKT_CLIENT_SECRET",
        value = "\"${properties["TIVI_TRAKT_CLIENT_SECRET"]?.toString() ?: ""}\"",
    )
    buildConfigField(
        type = "String",
        name = "TRAKT_CLIENT_ID",
        value = "\"${properties["TIVI_TRAKT_CLIENT_ID"]?.toString() ?: ""}\"",
    )
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)
                implementation(projects.core.logging.api)

                api(libs.trakt.api)
                api(projects.data.traktauth)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.auth)

                api(libs.kotlin.coroutines.core)

                api(libs.kotlininject.runtime)
            }
        }

        val jvmMain by getting {
            dependencies {
                api(libs.okhttp.okhttp)
                implementation(libs.ktor.client.okhttp)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}
