// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.kotlin.multiplatform")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.core.base)
        api(libs.kotlinx.datetime)
      }
    }
  }
}
