// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

android {
  namespace = "app.tivi.showdetails.details"
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.base)
        implementation(projects.domain)
        implementation(projects.common.ui.compose)

        api(projects.common.ui.screens)
        api(libs.circuit.foundation)
        implementation(libs.circuit.retained)
        implementation(projects.common.ui.circuit)

        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.material3)
        implementation(libs.compose.material3.windowsizeclass)
        implementation(compose.animation)
      }
    }
  }
}
