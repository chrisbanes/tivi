/*
 * Copyright 2019 Google LLC
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


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.cacheFixPlugin)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "app.tivi.domain"
}

dependencies {
    implementation(projects.base)

    api(projects.data.models)
    implementation(projects.data.db) // remove this eventually
    implementation(projects.data.legacy) // remove this eventually

    api(projects.data.episodes)
    api(projects.data.followedshows)
    api(projects.data.popularshows)
    api(projects.data.recommendedshows)
    api(projects.data.relatedshows)
    api(projects.data.search)
    api(projects.data.showimages)
    api(projects.data.shows)
    api(projects.data.traktusers)
    api(projects.data.trendingshows)
    api(projects.data.watchedshows)

    api(projects.api.trakt) // TraktAuthState
    implementation(projects.api.traktAuth)
    implementation(projects.api.tmdb)

    implementation(libs.hilt.library)
    kapt(libs.hilt.compiler)

    api(libs.androidx.paging.common)
    implementation(libs.androidx.paging.runtime)
}
