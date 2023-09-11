// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
    kotlin("plugin.serialization") version "1.9.10"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.base)
                implementation(projects.core.logging.api)
                implementation(libs.kotlinx.datetime)
                api(projects.data.models)
                implementation(projects.data.db)
                implementation(projects.data.legacy) // remove this eventually

                implementation(projects.api.trakt)
                implementation(projects.api.tmdb)

                api(libs.store)
                implementation(libs.kotlinx.atomicfu)

                implementation(libs.androidx.collection) // LruCache

                implementation(libs.kotlininject.runtime)
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
    namespace = "app.tivi.data.licenses"
}
