// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

android {
  namespace = "app.tivi.licenses"
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.core.base)
        implementation(projects.domain)
        implementation(projects.common.ui.compose)
        implementation(projects.common.ui.components)

        implementation(projects.data.licenses) // This should really be used through an interactor
        api(projects.common.ui.screens)

        api(libs.circuit.foundation)
        implementation(libs.circuit.retained)

        implementation(compose.material3)
        implementation(compose.animation)
      }
    }
  }
}
