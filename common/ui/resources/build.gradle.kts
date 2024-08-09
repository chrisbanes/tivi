// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(compose.ui)
        api(projects.data.models)
        api(compose.components.resources)
        implementation(compose.runtime)
      }
    }

    iosMain {
      dependencies {
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        implementation(compose.components.resources)
      }
    }

    commonTest {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.assertk)
      }
    }

    jvmTest {
      dependencies {
        implementation(compose.desktop.currentOs)
      }
    }
  }
}

android {
  namespace = "app.tivi.common.ui.resources"

  sourceSets["main"].apply {
    res.srcDirs("src/androidMain/res")
  }
}

compose.resources {
  publicResClass = true
  packageOfResClass = "app.tivi.common.ui.resources"
}
