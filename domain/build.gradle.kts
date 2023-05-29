// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.ksp)
}

// https://github.com/cashapp/multiplatform-paging/issues/6
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>> {
    compilerOptions.freeCompilerArgs.add("-opt-in=androidx.paging.ExperimentalPagingApi")
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.base)

                api(projects.data.models)
                implementation(projects.data.db) // remove this eventually
                api(projects.data.legacy) // remove this eventually

                api(projects.data.episodes)
                api(projects.data.followedshows)
                api(projects.data.popularshows)
                api(projects.data.recommendedshows)
                api(projects.data.relatedshows)
                api(projects.data.search)
                api(projects.data.showimages)
                api(projects.data.shows)
                api(projects.data.traktauth)
                api(projects.data.traktusers)
                api(projects.data.trendingshows)
                api(projects.data.watchedshows)

                implementation(projects.api.tmdb)

                api(libs.cashapp.paging.common)

                implementation(libs.kotlininject.runtime)
            }
        }
    }
}

dependencies {
    add("kspJvm", libs.kotlininject.compiler)
}
