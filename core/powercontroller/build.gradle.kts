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
                implementation(projects.core.base)
            }
        }

        val androidMain by getting {
            dependencies {
                api(projects.core.preferences)
                implementation(libs.androidx.core)

                implementation(libs.kotlininject.runtime)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.kotlininject.compiler)
}

android {
    namespace = "app.tivi.core.powercontroller"
}
