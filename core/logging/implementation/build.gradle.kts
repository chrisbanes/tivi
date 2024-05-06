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
        api(projects.core.base)
        api(projects.core.logging.api)
        api(projects.core.preferences)
        api(libs.kotlin.coroutines.core)
        implementation(libs.kermit.kermit)
        implementation(libs.kotlininject.runtime)
      }
    }

    androidMain {
      dependencies {
        implementation(libs.crashkios.crashlytics)
        implementation(libs.google.firebase.crashlytics)
        implementation(libs.timber)
      }
    }

    iosMain {
      dependencies {
        implementation(libs.crashkios.crashlytics)
      }
    }
  }
}

android {
  namespace = "app.tivi.core.logging"
}
