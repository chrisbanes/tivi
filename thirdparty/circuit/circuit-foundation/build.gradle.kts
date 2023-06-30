// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
                api(compose.foundation)
                api(libs.kotlin.coroutines.core)
                api(projects.thirdparty.circuit.backstack)
                api(projects.thirdparty.circuit.circuitRuntime)
                api(projects.thirdparty.circuit.circuitRuntimePresenter)
                api(projects.thirdparty.circuit.circuitRuntimeUi)
                api(compose.ui)
            }
        }
        maybeCreate("androidMain").apply {
            dependencies {
                api(compose.animation)
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}

tasks
  .withType<KotlinCompile>()
  .matching { it.name.contains("test", ignoreCase = true) }
  .configureEach {
    compilerOptions { freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi") }
  }

android {
    namespace = "com.slack.circuit.foundation"
}
