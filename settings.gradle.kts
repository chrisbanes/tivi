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
    id("com.gradle.enterprise") version "3.11.4"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":base",
    ":core:analytics",
    ":core:logging",
    ":common:ui:view",
    ":common:ui:resources",
    ":common:ui:compose",
    ":common:imageloading",
    ":data",
    ":data-android",
    ":api:trakt",
    ":api:trakt-auth",
    ":api:tmdb",
    ":tasks",
    ":domain",
    ":ui:discover",
    ":ui:showdetails",
    ":ui:episodedetails",
    ":ui:followed",
    ":ui:watched",
    ":ui:trending",
    ":ui:popular",
    ":ui:recommended",
    ":ui:search",
    ":ui:showseasons",
    ":ui:settings",
    ":ui:account",
    ":app",
    ":benchmark",
)
