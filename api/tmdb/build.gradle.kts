// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.kotlin.multiplatform")
  alias(libs.plugins.buildConfig)
}

buildConfig {
  packageName("app.tivi.tmdb")

  buildConfigField(
    type = String::class.java,
    name = "TMDB_DEBUG_API_KEY",
    value = provider { properties["TIVI_DEBUG_TMDB_API_KEY"]?.toString() ?: "" },
  )
  buildConfigField(
    type = String::class.java,
    name = "TMDB_API_KEY",
    value = provider { properties["TIVI_TMDB_API_KEY"]?.toString() ?: "" },
  )
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(projects.core.base)
        implementation(projects.core.logging.api)

        api(libs.tmdb.api)
        implementation(libs.ktor.client.core)

        api(libs.kotlin.coroutines.core)

        api(libs.kotlininject.runtime)
      }
    }

    val jvmMain by getting {
      dependencies {
        api(libs.okhttp.okhttp)
        implementation(libs.ktor.client.okhttp)
      }
    }

    val iosMain by getting {
      dependencies {
        implementation(libs.ktor.client.darwin)
      }
    }
  }
}
