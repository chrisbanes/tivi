// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.buildConfig)
}

buildConfig {
    packageName("app.tivi.tmdb")

    buildConfigField("String", "TMDB_DEBUG_API_KEY", "\"${propOrDef("TIVI_DEBUG_TMDB_API_KEY", "")}\"")
    buildConfigField("String", "TMDB_API_KEY", "\"${propOrDef("TIVI_TMDB_API_KEY", "")}\"")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)

                api(libs.tmdb.api)
                implementation(libs.ktor.client.core)

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
