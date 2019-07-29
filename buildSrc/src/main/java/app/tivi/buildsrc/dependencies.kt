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
    const val ktlint = "0.33.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:3.5.0-rc01"
    const val dexcountGradlePlugin = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.8.6"
    const val playPublisherPlugin = "com.github.triplet.gradle:play-publisher:2.2.1"

    const val mvRx = "com.airbnb.android:mvrx:1.0.1"

    const val threeTenBp = "org.threeten:threetenbp:1.4.0"
    const val threeTenBpNoTzdb = "$threeTenBp:no-tzdb"
    const val threeTenAbp = "com.jakewharton.threetenabp:threetenabp:1.2.1"

    const val gravitySnapHelper = "com.github.rubensousa:gravitysnaphelper:2.0"

    const val rxLint = "nl.littlerobots.rxlint:rxlint:1.7.4"

    const val timber = "com.jakewharton.timber:timber:4.7.1"

    const val tmdbJava = "com.uwetrottmann.tmdb2:tmdb-java:2.0.3"
    const val traktJava = "com.uwetrottmann.trakt5:trakt-java:6.2.0"

    const val appauth = "net.openid:appauth:0.7.1"

    const val junit = "junit:junit:4.12"
    const val robolectric = "org.robolectric:robolectric:4.3"
    const val mockK = "io.mockk:mockk:1.9.3"

    object Google {
        const val material = "com.google.android.material:material:1.1.0-alpha07"
        const val firebaseCore = "com.google.firebase:firebase-core:17.0.0"
        const val crashlytics = "com.crashlytics.sdk.android:crashlytics:2.10.1"
        const val gmsGoogleServices = "com.google.gms:google-services:4.3.0"
        const val fabricPlugin = "io.fabric.tools:gradle:1.29.0"
    }

    object Kotlin {
        private const val version = "1.3.41"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }

    object Coroutines {
        private const val version = "1.2.1"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val rx2 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    }

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.1.0-beta01"
        const val browser = "androidx.browser:browser:1.0.0"
        const val palette = "androidx.palette:palette:1.0.0"
        const val recyclerview = "androidx.recyclerview:recyclerview:1.1.0-alpha06"
        const val emoji = "androidx.emoji:emoji:1.0.0"
        const val swiperefresh = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0-alpha02"

        object Navigation {
            private const val version = "2.1.0-alpha05"
            const val fragment = "androidx.navigation:navigation-fragment-ktx:$version"
            const val ui = "androidx.navigation:navigation-ui-ktx:$version"
            const val safeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:$version"
        }

        object Fragment {
            private const val version = "1.2.0-alpha01"
            const val fragment = "androidx.fragment:fragment:$version"
            const val fragmentKtx = "androidx.fragment:fragment-ktx:$version"
        }

        object Test {
            private const val version = "1.2.0"
            const val core = "androidx.test:core:$version"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"

            const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
        }

        const val archCoreTesting = "androidx.arch.core:core-testing:2.0.1"

        object Paging {
            private const val version = "2.1.0"
            const val common = "androidx.paging:paging-common:$version"
            const val runtime = "androidx.paging:paging-runtime:$version"
            const val rxjava2 = "androidx.paging:paging-rxjava2:$version"
        }

        const val preference = "androidx.preference:preference:1.1.0-beta01"

        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.0-beta2"

        const val coreKtx = "androidx.core:core-ktx:1.2.0-alpha02"

        object Lifecycle {
            private const val version = "2.2.0-alpha02"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
            const val reactive = "androidx.lifecycle:lifecycle-reactivestreams-ktx:$version"
            const val compiler = "androidx.lifecycle:lifecycle-compiler:$version"
            const val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        }

        object Room {
            private const val version = "2.2.0-alpha01"
            const val common = "androidx.room:room-common:$version"
            const val runtime = "androidx.room:room-runtime:$version"
            const val rxjava2 = "androidx.room:room-rxjava2:$version"
            const val compiler = "androidx.room:room-compiler:$version"
            const val ktx = "androidx.room:room-ktx:$version"
            const val testing = "androidx.room:room-testing:$version"
        }

        object Work {
            private const val version = "2.1.0"
            const val runtimeKtx = "androidx.work:work-runtime-ktx:$version"
        }
    }

    object RxJava {
        const val rxJava = "io.reactivex.rxjava2:rxjava:2.2.10"
        const val rxKotlin = "io.reactivex.rxjava2:rxkotlin:2.3.0"
        const val rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.1"
    }

    object Dagger {
        private const val version = "2.23.2"
        const val dagger = "com.google.dagger:dagger:$version"
        const val androidSupport = "com.google.dagger:dagger-android-support:$version"
        const val compiler = "com.google.dagger:dagger-compiler:$version"
        const val androidProcessor = "com.google.dagger:dagger-android-processor:$version"
    }

    object Glide {
        private const val version = "4.9.0"
        const val glide = "com.github.bumptech.glide:glide:$version"
        const val compiler = "com.github.bumptech.glide:compiler:$version"
    }

    object Retrofit {
        private const val version = "2.6.0"
        const val retrofit = "com.squareup.retrofit2:retrofit:$version"
        const val retrofit_rxjava_adapter = "com.squareup.retrofit2:adapter-rxjava2:$version"
        const val gsonConverter = "com.squareup.retrofit2:converter-gson:$version"
    }

    object OkHttp {
        private const val version = "4.0.0"
        const val okhttp = "com.squareup.okhttp3:okhttp:$version"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Epoxy {
        private const val version = "3.7.0"
        const val epoxy = "com.airbnb.android:epoxy:$version"
        const val paging = "com.airbnb.android:epoxy-paging:$version"
        const val dataBinding = "com.airbnb.android:epoxy-databinding:$version"
        const val processor = "com.airbnb.android:epoxy-processor:$version"
    }

    object AssistedInject {
        private const val version = "0.4.0"
        const val annotationDagger2 = "com.squareup.inject:assisted-inject-annotations-dagger2:$version"
        const val processorDagger2 = "com.squareup.inject:assisted-inject-processor-dagger2:$version"
    }
}
