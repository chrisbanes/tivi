// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  alias(libs.plugins.kotlin.parcelize)
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(libs.circuit.runtime)
      }
    }
  }

  targets.configureEach {
    val isAndroidTarget = platformType == KotlinPlatformType.androidJvm
    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions {
          if (isAndroidTarget) {
            freeCompilerArgs.addAll(
              "-P",
              "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=app.tivi.screens.Parcelize",
            )
          }
        }
      }
    }
  }
}

android {
  namespace = "app.tivi.screens"
}
