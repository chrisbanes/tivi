/*
 * Copyright 2019 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package app.tivi.buildsrc

object Versions {
    val ktlint = "0.31.0"
}

object Libs {
    val androidGradlePlugin = "com.android.tools.build:gradle:3.5.0-beta05"
    val dexcountGradlePlugin = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.8.6"
    val playPublisherPlugin = "com.github.triplet.gradle:play-publisher:2.2.1"

    val mvRx = "com.airbnb.android:mvrx:1.0.2"

    val threeTenBp = "org.threeten:threetenbp:1.4.0"
    val threeTenBpNoTzdb = "$threeTenBp:no-tzdb"
    val threeTenAbp = "com.jakewharton.threetenabp:threetenabp:1.2.1"

    val gravitySnapHelper = "com.github.rubensousa:gravitysnaphelper:2.0"

    val rxLint = "nl.littlerobots.rxlint:rxlint:1.7.4"

    val timber = "com.jakewharton.timber:timber:4.7.1"

    val tmdbJava = "com.uwetrottmann.tmdb2:tmdb-java:2.0.3"
    val traktJava = "com.uwetrottmann.trakt5:trakt-java:6.2.0"

    val appauth = "net.openid:appauth:0.7.1"

    val junit = "junit:junit:4.12"
    val robolectric = "org.robolectric:robolectric:4.3"
    val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0"

    object Google {
        val material = "com.google.android.material:material:1.1.0-alpha07"
        val firebaseCore = "com.google.firebase:firebase-core:17.0.0"
        val crashlytics = "com.crashlytics.sdk.android:crashlytics:2.10.1"
        val gmsGoogleServices = "com.google.gms:google-services:4.2.0"
        val fabricPlugin = "io.fabric.tools:gradle:1.29.0"
    }

    object Kotlin {
        private const val version = "1.3.40"
        val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
        val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }

    object Coroutines {
        private const val version = "1.2.1"
        val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        val rx2 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:$version"
        val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    }

    object AndroidX {
        val appcompat = "androidx.appcompat:appcompat:1.1.0-beta01"
        val browser = "androidx.browser:browser:1.0.0"
        val palette = "androidx.palette:palette:1.0.0"
        val recyclerview = "androidx.recyclerview:recyclerview:1.1.0-alpha06"
        val emoji = "androidx.emoji:emoji:1.0.0"

        object Navigation {
            private const val version = "2.1.0-alpha05"
            val fragment = "androidx.navigation:navigation-fragment-ktx:$version"
            val ui = "androidx.navigation:navigation-ui-ktx:$version"
            val safeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:$version"
        }

        object Fragment {
            private const val version = "1.1.0-beta01"
            val fragment = "androidx.fragment:fragment:$version"
            val fragmentKtx = "androidx.fragment:fragment-ktx:$version"
        }

        object Test {
            private const val version = "1.2.0"
            val core = "androidx.test:core:$version"
            val runner = "androidx.test:runner:$version"
            val rules = "androidx.test:rules:$version"

            val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
        }

        val archCoreTesting = "androidx.arch.core:core-testing:2.0.1"

        object Paging {
            private const val version = "2.1.0"
            val common = "androidx.paging:paging-common:$version"
            val runtime = "androidx.paging:paging-runtime:$version"
            val rxjava2 = "androidx.paging:paging-rxjava2:$version"
        }

        val preference = "androidx.preference:preference:1.1.0-beta01"

        val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.0-beta2"

        val coreKtx = "androidx.core:core-ktx:1.2.0-alpha02"

        object Lifecycle {
            private const val version = "2.1.0-beta01"
            val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
            val reactive = "androidx.lifecycle:lifecycle-reactivestreams-ktx:$version"
            val compiler = "androidx.lifecycle:lifecycle-compiler:$version"
            val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        }

        object Room {
            private const val version = "2.1.0"
            val common = "androidx.room:room-common:$version"
            val runtime = "androidx.room:room-runtime:$version"
            val rxjava2 = "androidx.room:room-rxjava2:$version"
            val compiler = "androidx.room:room-compiler:$version"
            val ktx = "androidx.room:room-ktx:$version"
            val testing = "androidx.room:room-testing:$version"
        }

        object Work {
            private const val version = "2.1.0-rc01"
            val runtimeKtx = "androidx.work:work-runtime-ktx:$version"
        }
    }

    object RxJava {
        val rxJava = "io.reactivex.rxjava2:rxjava:2.2.10"
        val rxKotlin = "io.reactivex.rxjava2:rxkotlin:2.3.0"
        val rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.1"
    }

    object Dagger {
        private const val version = "2.23.2"
        val dagger = "com.google.dagger:dagger:$version"
        val androidSupport = "com.google.dagger:dagger-android-support:$version"
        val compiler = "com.google.dagger:dagger-compiler:$version"
        val androidProcessor = "com.google.dagger:dagger-android-processor:$version"
    }

    object Glide {
        private const val version = "4.9.0"
        val glide = "com.github.bumptech.glide:glide:$version"
        val compiler = "com.github.bumptech.glide:compiler:$version"
    }

    object Retrofit {
        private const val version = "2.6.0"
        val retrofit = "com.squareup.retrofit2:retrofit:$version"
        val retrofit_rxjava_adapter = "com.squareup.retrofit2:adapter-rxjava2:$version"
        val gsonConverter = "com.squareup.retrofit2:converter-gson:$version"
    }

    object OkHttp {
        private const val version = "4.0.0-RC3"
        val okhttp = "com.squareup.okhttp3:okhttp:$version"
        val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Epoxy {
        private const val version = "3.6.0"
        val epoxy = "com.airbnb.android:epoxy:$version"
        val paging = "com.airbnb.android:epoxy-paging:$version"
        val dataBinding = "com.airbnb.android:epoxy-databinding:$version"
        val processor = "com.airbnb.android:epoxy-processor:$version"
    }

    object AssistedInject {
        private const val version = "0.4.0"
        val annotationDagger2 = "com.squareup.inject:assisted-inject-annotations-dagger2:$version"
        val processorDagger2 = "com.squareup.inject:assisted-inject-processor-dagger2:$version"
    }
}
