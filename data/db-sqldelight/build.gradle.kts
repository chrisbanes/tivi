// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  alias(libs.plugins.sqldelight)
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.base)
        api(projects.data.db)

        api(libs.kotlinx.datetime)
        // Need to force upgrade these for recent Kotlin support
        api(libs.kotlinx.atomicfu)
        api(libs.kotlin.coroutines.core)

        implementation(libs.kotlininject.runtime)

        implementation(libs.sqldelight.coroutines)
        implementation(libs.sqldelight.primitive)
        implementation(libs.paging.common)
      }
    }

    androidMain {
      dependencies {
        api(libs.sqldelight.android)
      }
    }

    jvmMain {
      dependencies {
        api(libs.sqldelight.sqlite)
      }
    }

    iosMain {
      dependencies {
        api(libs.sqldelight.native)

        // Need to explicitly depend on these, otherwise the build fails.
        implementation("co.touchlab:stately-common:2.0.7")
        implementation("co.touchlab:stately-isolate:2.0.7")
        implementation("co.touchlab:stately-iso-collections:2.0.7")
      }
    }
  }
}

sqldelight {
  databases {
    create("Database") {
      packageName = "app.tivi.data"
    }
  }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    // Have to disable this as some of the generated code has
    // warnings for unused parameters
    allWarningsAsErrors = false
  }
}

android {
  namespace = "app.tivi.data.sqldelight"

  defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
}
