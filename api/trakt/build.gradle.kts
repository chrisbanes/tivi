// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.ksp)
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)

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
                implementation(libs.okhttp.okhttp)
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}

dependencies {
    add("kspJvm", libs.kotlininject.compiler)
}
