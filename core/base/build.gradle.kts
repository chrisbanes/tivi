// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.kotlin.multiplatform")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(libs.kotlin.coroutines.core)
        api(libs.kotlininject.runtime)
      }
    }

    jvmMain
  }
}
