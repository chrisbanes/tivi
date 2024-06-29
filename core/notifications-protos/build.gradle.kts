// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  alias(libs.plugins.wire)
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.core.base)
        api(libs.wire.runtime)
      }
    }
  }
}

wire {
  kotlin {}
}

android {
  namespace = "app.tivi.core.notifications.proto"
}
