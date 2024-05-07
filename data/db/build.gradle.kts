// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.kotlin.multiplatform")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.base)
        api(projects.data.models)
        api(libs.paging.common)
      }
    }
  }
}
