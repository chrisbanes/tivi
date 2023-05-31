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
                implementation(projects.core.analytics)
                implementation(projects.core.logging)

                implementation(projects.data.dbSqldelight)

                implementation(projects.data.followedshows)
                implementation(projects.data.episodes)
                implementation(projects.data.showimages)
                implementation(projects.data.shows)

                implementation(libs.kotlininject.runtime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(projects.data.legacy)

                implementation(kotlin("test"))
                implementation(libs.assertk)
                implementation(libs.kotlin.coroutines.test)

                implementation(libs.turbine)
            }
        }
    }
}

dependencies {
    add("kspJvmTest", libs.kotlininject.compiler)
    add("kspIosArm64Test", libs.kotlininject.compiler)
    add("kspIosSimulatorArm64Test", libs.kotlininject.compiler)
    add("kspIosX64Test", libs.kotlininject.compiler)
}
