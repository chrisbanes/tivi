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
        implementation(projects.core.base)
        implementation(projects.core.logging.api)
        implementation(projects.core.notifications.core)
        implementation(projects.core.entitlements.core)
        implementation(projects.domain)
        implementation(libs.kotlininject.runtime)
      }
    }

    androidMain {
      dependencies {
        api(libs.androidx.work.runtime)
      }
    }
  }
}

android {
  namespace = "app.tivi.tasks"

  defaultConfig {
    manifestPlaceholders += mapOf(
      "appAuthRedirectScheme" to "empty",
    )
  }
}
