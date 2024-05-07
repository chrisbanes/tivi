// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.core.base)
        api(projects.core.logging.api)
        implementation(libs.kotlinx.serialization)
      }
    }
  }
}

android {
  namespace = "app.tivi.data.licenses"
}
