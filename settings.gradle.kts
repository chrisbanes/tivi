// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


pluginManagement {
    includeBuild("gradle/build-logic")

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        mavenLocal()

        // Needed when using the 'dev' Compose Compiler
        // maven("https://androidx.dev/storage/compose-compiler/repository/")

        // Jetpack Compose SNAPSHOTs if needed
        // maven("https://androidx.dev/snapshots/builds/$composeSnapshot/artifacts/repository/")

        // Used for snapshots if needed
        // maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        // maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

plugins {
    id("com.gradle.enterprise") version "3.13.3"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// https://docs.gradle.org/7.6/userguide/configuration_cache.html#config_cache:stable
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "tivi"

include(
    ":core:analytics",
    ":core:base",
    ":core:logging",
    ":core:performance",
    ":core:powercontroller",
    ":core:preferences",
    ":common:ui:view",
    ":common:ui:resources",
    ":common:ui:resources-compose",
    ":common:ui:compose",
    ":common:imageloading",
    ":data:db",
    ":data:db-sqldelight",
    ":data:legacy",
    ":data:models",
    ":data:test",
    ":data:episodes",
    ":data:followedshows",
    ":data:popularshows",
    ":data:recommendedshows",
    ":data:relatedshows",
    ":data:search",
    ":data:shows",
    ":data:showimages",
    ":data:traktauth",
    ":data:traktusers",
    ":data:trendingshows",
    ":data:watchedshows",
    ":api:trakt",
    ":api:tmdb",
    ":tasks",
    ":domain",
    ":ui:discover",
    ":ui:episode:details",
    ":ui:episode:track",
    ":ui:trending",
    ":ui:popular",
    ":ui:recommended",
    ":ui:search",
    ":ui:show:details",
    ":ui:show:seasons",
    ":ui:settings",
    ":ui:library",
    ":ui:account",
    ":ui:upnext",
    ":android-app:app",
    ":android-app:benchmark",
)
