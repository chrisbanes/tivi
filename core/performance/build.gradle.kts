// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.base)
        implementation(libs.kotlininject.runtime)
      }
    }

    androidMain {
      dependencies {
        implementation(libs.google.firebase.perf)
      }
    }
  }
}

android {
  namespace = "app.tivi.core.perf"
}
