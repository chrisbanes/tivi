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
        implementation(projects.core.base)
        api(libs.multiplatformsettings.core)
        api(libs.multiplatformsettings.coroutines)
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(libs.androidx.core)
        implementation(libs.kotlininject.runtime)
      }
    }
  }
}

android {
  namespace = "app.tivi.core.preferences"
}
