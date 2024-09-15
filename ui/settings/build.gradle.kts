// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

android {
  namespace = "app.tivi.settings"
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.base)
        implementation(projects.core.entitlements.ui)
        implementation(projects.core.permissions)
        implementation(projects.core.preferences)
        implementation(projects.domain)
        implementation(projects.common.ui.compose)
        implementation(projects.common.ui.circuit)

        api(projects.common.ui.screens)
        api(libs.circuit.foundation)
        implementation(libs.circuit.overlay)
        implementation(libs.circuitx.overlays)
        implementation(libs.circuit.retained)

        implementation(compose.materialIconsExtended)
        implementation(compose.material3)
        implementation(compose.animation)
      }
    }
  }
}
