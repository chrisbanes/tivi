// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

android {
  namespace = "app.tivi.home.upnext"
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.core.base)
        implementation(projects.domain)
        implementation(projects.common.ui.compose)
        implementation(projects.common.ui.components)

        api(projects.common.ui.screens)
        api(projects.common.ui.circuitOverlay)
        api(libs.circuit.foundation)
        implementation(libs.circuit.retained)

        implementation(libs.paging.compose)

        implementation(projects.thirdparty.swipe)

        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.materialIconsExtended)
        implementation(compose.material3)
        implementation(compose.animation)
      }
    }
  }
}
