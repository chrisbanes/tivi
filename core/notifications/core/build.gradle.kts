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
        api(projects.core.permissions)
        implementation(projects.common.ui.resources.strings)
        api(libs.kotlinx.datetime)
      }
    }

    androidMain {
      dependencies {
        implementation(libs.androidx.core)
        implementation(libs.androidx.datastore)
        implementation(projects.core.notifications.protos)
      }
    }
  }
}

android {
  namespace = "app.tivi.core.notifications"
}
