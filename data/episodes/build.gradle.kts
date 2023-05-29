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
                api(projects.data.models)
                api(projects.data.traktauth)
                implementation(projects.data.db)
                implementation(projects.data.legacy) // remove this eventually

                api(projects.api.trakt)
                api(projects.api.tmdb)

                implementation(libs.kotlininject.runtime)
            }
        }
    }
}
