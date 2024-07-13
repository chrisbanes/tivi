// Copyright 2024, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

android {
  namespace = "app.tivi.developer.notifications"
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.base)
        api(projects.core.notifications.core)
        api(projects.domain)

        implementation(projects.common.ui.compose)

        api(projects.common.ui.screens)
        api(libs.circuit.foundation)
        implementation(libs.circuit.retained)

        implementation(compose.material3)
        implementation(compose.animation)
      }
    }
  }
}
