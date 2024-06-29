// Copyright 2024, Christopher Banes and the Tivi project contributors
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
      }
    }

    val mokoImplMain by creating {
      dependsOn(commonMain.get())

      dependencies {
        implementation(libs.moko.permissions.core)
        api(projects.core.logging.api)
      }
    }

    androidMain {
      dependsOn(mokoImplMain)

      dependencies {
        api(libs.androidx.activity.activity)
      }
    }

    iosMain {
      dependsOn(mokoImplMain)
    }
  }
}

android {
  namespace = "app.tivi.core.permissions"
}
