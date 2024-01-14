// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.core.base)
        implementation(projects.core.logging.api)
        implementation(projects.domain)
        implementation(libs.kotlininject.runtime)
      }
    }

    val androidMain by getting {
      dependencies {
        api(libs.androidx.work.runtime)
      }
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
  namespace = "app.tivi.tasks"

  defaultConfig {
    manifestPlaceholders += mapOf(
      "appAuthRedirectScheme" to "empty",
    )
  }
}
