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
                api(compose.ui)
                api(libs.kotlin.coroutines.core)
                implementation(libs.uuid)
            }
        }
        maybeCreate("androidMain").apply {
            dependencies {
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                api(libs.androidx.lifecycle.viewmodel.ktx)
                api(compose.runtime)
            }
        }
    }
}

android {
    namespace = "com.slack.circuit.backstack"
}
