// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import app.tivi.gradle.addKspDependencyForAllTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    targets.withType<KotlinNativeTarget> {
        binaries.withType<Framework> {
            isStatic = true
            baseName = "TiviKt"

            export(projects.ui.root)
            export(projects.core.analytics)
            export(projects.data.traktauth)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.base)
                api(projects.core.analytics)
                api(projects.core.logging)
                api(projects.core.performance)
                api(projects.core.powercontroller)
                api(projects.core.preferences)
                api(projects.data.dbSqldelight)
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
                api(projects.ui.upnext)
            }
        }

        val jvmMain by getting {
            dependencies {
                api(libs.okhttp.okhttp)
            }
        }
    }
}

android {
    namespace = "app.tivi.shared"
}

ksp {
    arg("me.tatarka.inject.generateCompanionExtensions", "true")
}

addKspDependencyForAllTargets(libs.kotlininject.compiler)
