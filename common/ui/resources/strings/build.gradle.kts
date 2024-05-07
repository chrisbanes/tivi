// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.data.models)
        api(libs.lyricist.core)
      }
    }

    commonTest {
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
