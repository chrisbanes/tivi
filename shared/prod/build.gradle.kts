// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import app.tivi.gradle.addKspDependencyForAllTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
  alias(libs.plugins.ksp)
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.shared.common)
      }
    }

    targets.withType<KotlinNativeTarget>().configureEach {
      binaries.framework {
        isStatic = true
        baseName = "TiviKt"

        export(projects.ui.root)
        export(projects.core.analytics)
        export(projects.data.traktauth)
      }
    }
  }
}

android {
  namespace = "app.tivi.shared.prod"
}

ksp {
  arg("me.tatarka.inject.generateCompanionExtensions", "true")
}

addKspDependencyForAllTargets(libs.kotlininject.compiler)
