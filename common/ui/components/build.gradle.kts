// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.common.ui.compose)

        api(projects.data.models)
        api(projects.core.preferences)
        api(projects.common.imageloading)

        api(libs.circuit.foundation)

        api(libs.haze)
        api(libs.coil.compose)

        implementation(compose.foundation)
        implementation(compose.materialIconsExtended)
        implementation(compose.animation)

        // Only used for Pull-to-refresh
        implementation(compose.material)
        api(compose.material3)

        api(libs.cupertino.adaptive)

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
    }
  }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    // Have to disable this due to 'duplicate library name'
    // https://youtrack.jetbrains.com/issue/KT-51110
    allWarningsAsErrors = false
  }
}

android {
  namespace = "app.tivi.common.components"

  lint {
    baseline = file("lint-baseline.xml")
  }
}
