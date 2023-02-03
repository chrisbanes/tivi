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
    id("com.gradle.enterprise") version "3.12.3"
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

include(
    ":base",
    ":core:analytics",
    ":core:logging",
    ":common:ui:view",
    ":common:ui:resources",
    ":common:ui:compose",
    ":common:imageloading",
    ":data:legacy",
    ":data:legacy-test",
    ":data:legacy-inject",
    ":data:room",
    ":api:trakt",
    ":api:trakt-auth",
    ":api:tmdb",
    ":tasks",
    ":domain",
    ":ui:discover",
    ":ui:showdetails",
    ":ui:episodedetails",
    ":ui:trending",
    ":ui:popular",
    ":ui:recommended",
    ":ui:search",
    ":ui:showseasons",
    ":ui:settings",
    ":ui:library",
    ":ui:account",
    ":ui:upnext",
    ":app",
    ":benchmark",
)
