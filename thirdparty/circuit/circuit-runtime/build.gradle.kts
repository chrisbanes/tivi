// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    // region KMP Targets
    android { publishLibraryVariants("release") }
    jvm()
    ios()
    iosSimulatorArm64()
    // endregion

    sourceSets {
        commonMain {
            dependencies {
                api(compose.runtime)
            }
        }
        val iosMain by getting
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

android { namespace = "com.slack.circuit.runtime" }
