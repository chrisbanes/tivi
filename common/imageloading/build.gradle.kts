// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)
                implementation(projects.core.logging)
                implementation(projects.core.powercontroller)

                implementation(projects.data.models)
                implementation(projects.data.episodes)
                implementation(projects.data.showimages)

                implementation(projects.api.tmdb)

                implementation(libs.kotlininject.runtime)

                api(libs.imageloader)
            }
        }
    }
}

android {
    namespace = "app.tivi.common.imageloading"
}
