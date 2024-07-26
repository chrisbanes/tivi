// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import app.tivi.gradle.addKspDependencyForAllTargets

plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
  alias(libs.plugins.ksp)
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.core.base)
        api(projects.core.analytics)
        api(projects.core.entitlements.core)
        api(projects.core.logging.implementation)
        api(projects.core.notifications.core)
        api(projects.core.performance)
        api(projects.core.permissions)
        api(projects.core.powercontroller)
        api(projects.core.preferences)
        api(projects.data.dbSqldelight)
        api(projects.data.licenses)
        api(projects.api.trakt)
        api(projects.api.tmdb)
        api(projects.domain)
        api(projects.tasks)

        api(projects.common.imageloading)
        api(projects.common.ui.compose)

        api(projects.ui.account)
        api(projects.ui.discover)
        api(projects.ui.episode.details)
        api(projects.ui.episode.track)
        api(projects.ui.library)
        api(projects.ui.popular)
        api(projects.ui.trending)
        api(projects.ui.recommended)
        api(projects.ui.search)
        api(projects.ui.show.details)
        api(projects.ui.show.seasons)
        api(projects.ui.root)
        api(projects.ui.settings)
        api(projects.ui.licenses)
        api(projects.ui.upnext)
      }
    }

    jvmMain {
      dependencies {
        api(libs.okhttp.okhttp)
      }
    }
  }
}

android {
  namespace = "app.tivi.shared.common"
}

ksp {
  arg("me.tatarka.inject.generateCompanionExtensions", "true")
}

addKspDependencyForAllTargets(libs.kotlininject.compiler)
