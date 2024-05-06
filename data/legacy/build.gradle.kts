// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.kotlin.multiplatform")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.core.base)
        api(projects.api.trakt)
        api(projects.api.tmdb)
        api(projects.core.logging.api)
        api(projects.data.models)
        implementation(projects.data.db)

        api(libs.store)

        implementation(libs.kotlininject.runtime)
      }
    }
  }
}
