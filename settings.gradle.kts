/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.13.2"
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
    ":tasks:api",
    ":tasks:android",
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
    ":app",
    ":benchmark",
)
