// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
}

kotlin {
  sourceSets {
    commonTest {
      dependencies {
        implementation(projects.data.dbSqldelight)

        implementation(projects.data.followedshows)
        implementation(projects.data.episodes)
        implementation(projects.data.showimages)
        implementation(projects.data.shows)
        implementation(projects.data.legacy)

        implementation(kotlin("test"))
        implementation(libs.assertk)
        implementation(libs.kotlin.coroutines.test)

        implementation(libs.turbine)

        implementation(libs.uuid)
      }
    }

    androidUnitTest {
      dependencies {
        implementation(libs.sqldelight.sqlite)
      }
    }
  }
}

android {
  namespace = "app.tivi.data.test"
}
