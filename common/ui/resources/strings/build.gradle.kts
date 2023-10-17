// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.data.models)
        api(libs.lyricist.core)
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.assertk)
      }
    }
  }
}

android {
  namespace = "app.tivi.common.ui.resources.strings"
}
