// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

android {
  namespace = "app.tivi.ui.overlays"
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.common.ui.compose)
        implementation(projects.common.ui.screens)

        implementation(compose.material3)
        implementation(compose.animation)

        api(libs.uri)

        api(libs.circuit.foundation)
        api(libs.circuit.overlay)
      }
    }
  }
}
