// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

android {
  namespace = "app.tivi.home"
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.base)
        implementation(projects.core.analytics)
        implementation(projects.common.ui.compose)

        implementation(libs.haze.materials)

        implementation(projects.domain)
        implementation(projects.data.traktauth)

        implementation(projects.common.ui.screens)
        implementation(libs.circuit.foundation)
        implementation(libs.circuit.retained)
        implementation(libs.circuitx.gestureNavigation)
        implementation(libs.circuit.overlay)
        implementation(projects.common.ui.circuit)

        implementation(compose.foundation)
        implementation(compose.materialIconsExtended)
      }
    }

    androidMain {
      dependencies {
        implementation(libs.androidx.activity.compose)
      }
    }
  }
}
