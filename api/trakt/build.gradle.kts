// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.buildConfig)
}

buildConfig {
    packageName("app.tivi.trakt")

    buildConfigField("String", "TRAKT_DEBUG_CLIENT_SECRET", "\"${propOrDef("TIVI_DEBUG_TRAKT_CLIENT_SECRET", "")}\"")
    buildConfigField("String", "TRAKT_DEBUG_CLIENT_ID", "\"${propOrDef("TIVI_DEBUG_TRAKT_CLIENT_ID", "")}\"")
    buildConfigField("String", "TRAKT_CLIENT_SECRET", "\"${propOrDef("TIVI_TRAKT_CLIENT_SECRET", "")}\"")
    buildConfigField("String", "TRAKT_CLIENT_ID", "\"${propOrDef("TIVI_TRAKT_CLIENT_ID", "")}\"")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)
                implementation(projects.core.logging)

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

fun <T : Any> propOrDef(propertyName: String, defaultValue: T): T {
    @Suppress("UNCHECKED_CAST")
    return project.properties[propertyName] as T? ?: defaultValue
}
