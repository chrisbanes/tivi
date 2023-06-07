// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)
                implementation(libs.kotlininject.runtime)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.google.firebase.perf)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.kotlininject.compiler)
}

android {
    namespace = "app.tivi.core.perf"
}
