// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(libs.paging.common)
        api(compose.runtime)
      }
    }
  }
}

android {
  namespace = "androidx.paging.compose"
}
