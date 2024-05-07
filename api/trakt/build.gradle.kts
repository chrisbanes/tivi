// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.kotlin.multiplatform")
  alias(libs.plugins.buildConfig)
}

buildConfig {
  packageName("app.tivi.trakt")

  buildConfigField(
    type = String::class.java,
    name = "TRAKT_DEBUG_CLIENT_SECRET",
    value = provider { properties["TIVI_DEBUG_TRAKT_CLIENT_SECRET"]?.toString() ?: "" },
  )
  buildConfigField(
    type = String::class.java,
    name = "TRAKT_DEBUG_CLIENT_ID",
    value = provider { properties["TIVI_DEBUG_TRAKT_CLIENT_ID"]?.toString() ?: "" },
  )
  buildConfigField(
    type = String::class.java,
    name = "TRAKT_CLIENT_SECRET",
    value = provider { properties["TIVI_TRAKT_CLIENT_SECRET"]?.toString() ?: "" },
  )
  buildConfigField(
    type = String::class.java,
    name = "TRAKT_CLIENT_ID",
    value = provider { properties["TIVI_TRAKT_CLIENT_ID"]?.toString() ?: "" },
  )
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.base)
        implementation(projects.core.logging.api)

        api(libs.trakt.api)
        api(projects.data.traktauth)

        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.auth)

        api(libs.kotlin.coroutines.core)

        api(libs.kotlininject.runtime)
      }
    }

    jvmMain {
      dependencies {
        api(libs.okhttp.okhttp)
        implementation(libs.ktor.client.okhttp)
      }
    }

    iosMain {
      dependencies {
        implementation(libs.ktor.client.darwin)
      }
    }
  }
}
