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
        api(projects.core.base)
        implementation(projects.core.logging.api)
        implementation(libs.kotlinx.datetime)
      }
    }

    androidMain {
      dependencies {
        api(libs.appauth)

        implementation(libs.androidx.activity.activity)
        implementation(libs.androidx.browser)
        implementation(libs.androidx.core)

        implementation(libs.playservices.blockstore)
        implementation(libs.kotlinx.coroutines.playservices)

        implementation(libs.kotlininject.runtime)
      }
    }

    iosMain {
      dependencies {
        implementation(libs.multiplatformsettings.core)
      }
    }
  }
}

android {
  namespace = "app.tivi.data.traktauth"
}
