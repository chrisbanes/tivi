/*
 * Copyright 2018 Google, Inc.
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

object ApplicationId {
    val application_id = "app.tivi"
}

object Releases {
    val version_code = 17
    val version = "0.2.8.1"
}

object Modules {
    val base = ":base"
    val baseAndroid = ":base-android"
    val data = ":data"
    val dataAndroid = ":data-android"
    val datasources = ":datasources"
    val interactors = ":interactors"
    val tasks = ":tasks"
    val tmdb = ":tmdb"
    val trakt = ":trakt"
    val traktAuth = ":trakt-auth"
}

object Urls {
    val BASE_URL = ""
}

object BuildConfigs {
    val compileSdk = 28
    val minSdk = 23
    val targetSdk = 28
}

object Versions {
    val android_gradle = "3.2.0-beta05"
    val kotlin_version = "1.2.60"

    object Androidx {
        val appcompat = "28.0.0-rc01"
        val browser = Androidx.appcompat
        val core = "1.1.1"
        val constraint_layout = "2.0.0-alpha2"
        val core_ktx = "0.3"
        val fragment = Androidx.appcompat
        val emoji = Androidx.appcompat
        val lifecycle = Androidx.core
        val paging = "1.0.1"
        val palette = Androidx.appcompat
        val recyclerview = Androidx.appcompat
        val room = Androidx.core
        val test_runner = "1.0.2"
        val test_rules = Androidx.test_runner
        val work = "1.0.0-alpha06"
    }

    val coroutines = "0.24.0"
    val crashlytics = "2.9.3"
    val dagger = "2.17"
    val dexmaker = "2.19.1"
    val epoxy = "2.16.1"
    val espresso = "3.0.2"
    val firebase = "16.0.1"
    val glide = "4.7.1"
    val junit = "4.12"
    val kotlin = kotlin_version
    val ktlint = "0.19.0"
    val logging = "3.11.0"
    val mockito = "2.18.3"
    val material_design = Androidx.appcompat
    val openid = "0.7.1"
    val rx = "2.2.0"
    val rxkotlin = "2.3.0"
    val rxlint = "1.6.1"
    val rxandroid = "2.0.2"
    val rxbroadcast = "2.0.0"
    val threetenabp = "1.1.0"
    val threetenbp = "1.3.6:no-tzdb"
    val timber = "4.7.1"
    val tmdb2 = "1.9.0"
    val retrofit = "2.4.0"
}

object Libraries {
    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    val kotlin_jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"

    val lifecycle_extensions = "android.arch.lifecycle:extensions:${Versions.Androidx.lifecycle}"
    val lifecycle_reactivestreams = "android.arch.lifecycle:reactivestreams:${Versions.Androidx.lifecycle}"
    val lifecycle_compiler = "android.arch.lifecycle:compiler:${Versions.Androidx.lifecycle}"

    val paging_runtime = "android.arch.paging:runtime:${Versions.Androidx.paging}"
    val paging_rxjava2 = "android.arch.paging:rxjava2:${Versions.Androidx.paging}"
    val paging_common = "android.arch.paging:common:${Versions.Androidx.paging}"

    val appcompat = "com.android.support:appcompat-v7:${Versions.Androidx.appcompat}"
    val design = "com.android.support:design:${Versions.material_design}"
    val customtabs = "com.android.support:customtabs:${Versions.Androidx.browser}"
    val palette = "com.android.support:palette-v7:${Versions.Androidx.palette}"
    val recyclerview = "com.android.support:recyclerview-v7:${Versions.Androidx.recyclerview}"
    val emoji = "com.android.support:support-emoji:${Versions.Androidx.emoji}"
    val work = "android.arch.work:work-runtime-ktx:${Versions.Androidx.work}"
    val constraint_layout = "com.android.support.constraint:constraint-layout:${Versions.Androidx.constraint_layout}"
    val core_ktx = "androidx.core:core-ktx:${Versions.Androidx.core_ktx}"
    val threetenabp = "com.jakewharton.threetenabp:threetenabp:${Versions.threetenabp}"
    val rxjava = "io.reactivex.rxjava2:rxjava:${Versions.rx}"
    val rxkotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxkotlin}"
    val rxandroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxandroid}"

    val rxbroadcast = "com.cantrowitz:rxbroadcast:${Versions.rxbroadcast}"

    val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    val coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    val coroutines_rx2 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${Versions.coroutines}"

    val room = "android.arch.persistence.room:common:${Versions.Androidx.room}"

    val dagger = "com.google.dagger:dagger-android:${Versions.dagger}"
    val dagger_android = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    val dagger_compiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    val dagger_processor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"

    val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    val glide_compiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    val epoxy = "com.airbnb.android:epoxy:${Versions.epoxy}"
    val epoxy_paging = "com.airbnb.android:epoxy-paging:${Versions.epoxy}"
    val epoxy_databinding = "com.airbnb.android:epoxy-databinding:${Versions.epoxy}"
    val epoxy_processor = "com.airbnb.android:epoxy-processor:${Versions.epoxy}"

    val rxlint = "nl.littlerobots.rxlint:rxlint:${Versions.rxlint}"

    val firebase_core = "com.google.firebase:firebase-core:${Versions.firebase}"
    val crashlytics = "com.crashlytics.sdk.android:crashlytics:${Versions.crashlytics}"
    val logging = "com.squareup.okhttp3:logging-interceptor:${Versions.logging}"
    val fragment = "com.android.support:support-fragment:${Versions.Androidx.fragment}"
    val core_runtime = "android.arch.core:runtime:${Versions.Androidx.core}"
    val core_common = "android.arch.core:common:${Versions.Androidx.core}"
    val timber = "com.jakewharton.timber:timber:${Versions.timber}"
    val threetenbp = "org.threeten:threetenbp:${Versions.threetenbp}"
    val tmdb2 = "com.uwetrottmann.tmdb2:tmdb-java:${Versions.tmdb2}"
    val openid_appauth = "net.openid:appauth:${Versions.openid}"
}

object TestLibraries {
    val junit = "junit:junit:${Versions.junit}"

    val runner = "com.android.support.test:runner:${Versions.Androidx.test_runner}"
    val rules = "com.android.support.test:rules:${Versions.Androidx.test_rules}"
    val espresso = "com.android.support.test.espresso:espresso-core:${Versions.espresso}"
    val room = "android.arch.persistence.room:runtime:${Versions.Androidx.room}"
    val room_rxjava = "android.arch.persistence.room:rxjava2:${Versions.Androidx.room}"
    val room_compiler = "android.arch.persistence.room:compiler:${Versions.Androidx.room}"
    val mockito_dexmaker = "com.linkedin.dexmaker:dexmaker-mockito:${Versions.dexmaker}"
}

object Config {
    val testRunner = "android.support.test.runner.AndroidJUnitRunner"
}