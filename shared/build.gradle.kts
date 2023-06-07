// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.kotlin.multiplatform")
    id("app.tivi.android.library")
    alias(libs.plugins.ksp)
}

kotlin {
    jvm()
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.base)
                api(projects.core.analytics)
                api(projects.core.logging)
                api(projects.core.performance)
                api(projects.core.powercontroller)
                api(projects.core.preferences)
                api(projects.data.dbSqldelight)
                api(projects.api.trakt)
                api(projects.api.tmdb)
                api(projects.domain)
                api(projects.tasks)
            }
        }

        val androidMain by getting {
            dependencies {
                api(projects.common.imageloading)
                api(projects.common.ui.compose)
            }
        }
    }
}

android {
    namespace = "app.tivi.shared"
}
