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
        api(projects.core.preferences)
        api(libs.kotlinx.datetime)
        api(libs.kermit.kermit)

        implementation(libs.kotlininject.runtime)
      }
    }

    val mobileMain by creating {
      dependsOn(commonMain.get())

      dependencies {
        implementation(libs.crashkios.crashlytics)
      }
    }

    androidMain {
      dependsOn(mobileMain)

      dependencies {
        implementation(libs.google.firebase.crashlytics)
      }
    }

    iosMain {
      dependsOn(mobileMain)
    }
  }
}

android {
  namespace = "app.tivi.core.logging"
}
