// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  id("app.tivi.root")

  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.cacheFixPlugin) apply false
  alias(libs.plugins.android.lint) apply false
  alias(libs.plugins.android.test) apply false
  alias(libs.plugins.androidx.baselineprofile) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.parcelize) apply false
  alias(libs.plugins.gms.googleServices) apply false
  alias(libs.plugins.firebase.crashlytics) apply false
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.licensee) apply false
}

buildscript {
  dependencies {
    // Yuck. Need to force kotlinpoet:1.16.0 as that is what buildconfig uses.
    // CMP 1.6.0-x uses kotlinpoet:1.14.x. Gradle seems to force 1.14.x which then breaks
    // buildconfig.
    classpath("com.squareup:kotlinpoet:1.16.0")
  }
}

allprojects {
  tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
      // Treat all Kotlin warnings as errors
      allWarningsAsErrors = true

      if (project.providers.gradleProperty("tivi.enableComposeCompilerReports").isPresent) {
        freeCompilerArgs.addAll(
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
            layout.buildDirectory.asFile.get().absolutePath + "/compose_metrics",
        )
        freeCompilerArgs.addAll(
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
            layout.buildDirectory.asFile.get().absolutePath + "/compose_metrics",
        )
      }
    }
  }
}
