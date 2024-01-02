// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.data.models)
        api(projects.core.preferences)
        api(projects.common.imageloading)

        api(projects.common.ui.screens)
        api(libs.circuit.foundation)

        implementation(projects.common.ui.resources.fonts)
        api(projects.common.ui.resources.strings)
        api(libs.lyricist.library)

        api(libs.haze)
        api(libs.coil.compose)

        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.materialIconsExtended)
        api(compose.material3)
        api(libs.compose.material3.windowsizeclass)
        implementation(compose.animation)

        implementation(libs.uuid)

        implementation(libs.paging.compose)
      }
    }

    val jvmCommon by creating {
      dependsOn(commonMain)
    }

    val jvmMain by getting {
      dependsOn(jvmCommon)
    }

    val androidMain by getting {
      dependsOn(jvmCommon)

      dependencies {
        implementation(libs.androidx.activity.compose)
      }
    }
  }
}

android {
  namespace = "app.tivi.common.compose"

  lint {
    baseline = file("lint-baseline.xml")
  }
}
