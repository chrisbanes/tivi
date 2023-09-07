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
    repositories {
        google()
        mavenCentral()
        mavenLocal()

        // Prerelease versions of Compose Multiplatform
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

        // Used for snapshots if needed
        // maven("https://oss.sonatype.org/content/repositories/snapshots/")
        // maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

plugins {
    id("com.gradle.enterprise") version "3.14.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"

        if (System.getenv().containsKey("CI")) {
            // Always publish on CI
            publishAlways()
        }
    }
}

buildCache {
    val remoteBuildCacheUrl = providers.gradleProperty("REMOTE_BUILD_CACHE_URL").orNull ?: return@buildCache
    val isCi = System.getenv().containsKey("CI")

    local {
        isEnabled = !isCi
    }

    remote(HttpBuildCache::class) {
        url = uri(remoteBuildCacheUrl)
        isPush = isCi

        println("Enabling remote build cache. URL: $url. Push enabled: $isPush")

        credentials {
            username = extra["REMOTE_BUILD_CACHE_USERNAME"]?.toString()
            password = extra["REMOTE_BUILD_CACHE_PASSWORD"]?.toString()
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// https://docs.gradle.org/7.6/userguide/configuration_cache.html#config_cache:stable
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "tivi"

include(
    ":core:analytics",
    ":core:base",
    ":core:logging:api",
    ":core:logging:implementation",
    ":core:performance",
    ":core:powercontroller",
    ":core:preferences",
    ":common:ui:circuit-overlay",
    ":common:ui:resources:fonts",
    ":common:ui:resources:strings",
    ":common:ui:compose",
    ":common:ui:screens",
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
    ":shared:common",
    ":shared:qa",
    ":shared:prod",
    ":ui:developer:log",
    ":ui:developer:settings",
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
    ":ui:root",
    ":android-app:app",
    ":android-app:benchmark",
    ":android-app:common-test",
    ":desktop-app",
    ":thirdparty:swipe",
)
