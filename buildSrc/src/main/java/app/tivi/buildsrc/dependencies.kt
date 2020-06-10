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
    const val ktlint = "0.36.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:4.1.0-alpha10"

    const val mvRx = "com.airbnb.android:mvrx:1.3.0"

    const val threeTenBp = "org.threeten:threetenbp:1.4.4"
    const val threeTenBpNoTzdb = "$threeTenBp:no-tzdb"
    const val threeTenAbp = "com.jakewharton.threetenabp:threetenabp:1.2.4"

    const val gravitySnapHelper = "com.github.rubensousa:gravitysnaphelper:2.2.1"

    const val timber = "com.jakewharton.timber:timber:4.7.1"

    const val tmdbJava = "com.uwetrottmann.tmdb2:tmdb-java:2.2.0"
    const val traktJava = "com.uwetrottmann.trakt5:trakt-java:6.5.0"

    const val appauth = "net.openid:appauth:0.7.1"

    const val junit = "junit:junit:4.13"
    const val robolectric = "org.robolectric:robolectric:4.3.1"
    const val mockK = "io.mockk:mockk:1.10.0"

    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.3"

    const val store = "com.dropbox.mobile.store:store4:4.0.0-alpha05"

    object Insetter {
        private const val version = "0.2.2"
        const val dbx = "dev.chrisbanes:insetter-dbx:$version"
        const val ktx = "dev.chrisbanes:insetter-ktx:$version"
    }

    object Accompanist {
        private const val version = "0.1.5.ui-6574163-SNAPSHOT"
        const val mdcTheme = "dev.chrisbanes.accompanist:accompanist-mdc-theme:$version"
        const val coil = "dev.chrisbanes.accompanist:accompanist-coil:$version"
    }

    object Google {
        const val material = "com.google.android.material:material:1.1.0"
        const val firebaseCore = "com.google.firebase:firebase-core:17.4.3"

        const val crashlytics = "com.google.firebase:firebase-crashlytics:17.0.1"
        const val crashlyticsGradle = "com.google.firebase:firebase-crashlytics-gradle:2.1.1"

        const val gmsGoogleServices = "com.google.gms:google-services:4.3.3"

        const val openSourceLicensesPlugin = "com.google.android.gms:oss-licenses-plugin:0.10.2"
        const val openSourceLicensesLibrary = "com.google.android.gms:play-services-oss-licenses:17.0.0"
    }

    object Kotlin {
        private const val version = "1.3.72"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }

    object Coroutines {
        private const val version = "1.3.5"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.2.0-rc01"
        const val browser = "androidx.browser:browser:1.0.0"
        const val collection = "androidx.collection:collection-ktx:1.1.0"
        const val palette = "androidx.palette:palette:1.0.0"
        const val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"
        const val emoji = "androidx.emoji:emoji:1.1.0-rc01"
        const val swiperefresh = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0-rc01"

        object Navigation {
            private const val version = "2.2.2"
            const val fragment = "androidx.navigation:navigation-fragment-ktx:$version"
            const val ui = "androidx.navigation:navigation-ui-ktx:$version"
            const val safeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:$version"
        }

        object Fragment {
            private const val version = "1.2.3"
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

        const val archCoreTesting = "androidx.arch.core:core-testing:2.1.0"

        object Paging {
            private const val version = "2.1.2"
            const val common = "androidx.paging:paging-common-ktx:$version"
            const val runtime = "androidx.paging:paging-runtime-ktx:$version"
        }

        const val preference = "androidx.preference:preference:1.1.1"

        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.0-beta6"

        const val coreKtx = "androidx.core:core-ktx:1.3.0-rc01"

        object Lifecycle {
            private const val version = "2.2.0"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
            const val livedata = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        }

        object Room {
            private const val version = "2.2.5"
            const val common = "androidx.room:room-common:$version"
            const val runtime = "androidx.room:room-runtime:$version"
            const val compiler = "androidx.room:room-compiler:$version"
            const val ktx = "androidx.room:room-ktx:$version"
            const val testing = "androidx.room:room-testing:$version"
        }

        object Work {
            private const val version = "2.3.4"
            const val runtimeKtx = "androidx.work:work-runtime-ktx:$version"
        }

        object UI {
            const val snapshot = "6574163"
            const val version = "0.1.0-SNAPSHOT"

            const val core = "androidx.ui:ui-core:$version"
            const val layout = "androidx.ui:ui-layout:$version"
            const val material = "androidx.ui:ui-material:$version"
            const val foundation = "androidx.ui:ui-foundation:$version"
            const val animation = "androidx.ui:ui-animation:$version"
            const val tooling = "androidx.ui:ui-tooling:$version"
            const val livedata = "androidx.ui:ui-livedata:$version"
            const val iconsExtended = "androidx.ui:ui-material-icons-extended:$version"
        }

        object Compose {
            const val version = UI.version
            const val kotlinCompilerVersion = "1.3.70-dev-withExperimentalGoogleExtensions-20200424"

            const val runtime = "androidx.compose:compose-runtime:$version"
        }
    }

    object Dagger {
        private const val version = "2.28"
        const val dagger = "com.google.dagger:dagger:$version"
        const val androidSupport = "com.google.dagger:dagger-android-support:$version"
        const val compiler = "com.google.dagger:dagger-compiler:$version"
        const val androidProcessor = "com.google.dagger:dagger-android-processor:$version"
    }

    object Retrofit {
        private const val version = "2.8.1"
        const val retrofit = "com.squareup.retrofit2:retrofit:$version"
        const val retrofit_rxjava_adapter = "com.squareup.retrofit2:adapter-rxjava2:$version"
        const val gsonConverter = "com.squareup.retrofit2:converter-gson:$version"
    }

    object OkHttp {
        private const val version = "4.7.2"
        const val okhttp = "com.squareup.okhttp3:okhttp:$version"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Epoxy {
        private const val version = "3.9.0"
        const val epoxy = "com.airbnb.android:epoxy:$version"
        const val paging = "com.airbnb.android:epoxy-paging:$version"
        const val dataBinding = "com.airbnb.android:epoxy-databinding:$version"
        const val processor = "com.airbnb.android:epoxy-processor:$version"
    }

    object Coil {
        private const val version = "0.11.0"
        const val coil = "io.coil-kt:coil:$version"
    }

    object AssistedInject {
        private const val version = "0.5.2"
        const val annotationDagger2 = "com.squareup.inject:assisted-inject-annotations-dagger2:$version"
        const val processorDagger2 = "com.squareup.inject:assisted-inject-processor-dagger2:$version"
    }

    object Roomigrant {
        private const val version = "0.1.7"
        const val library = "com.github.MatrixDev.Roomigrant:RoomigrantLib:$version"
        const val compiler = "com.github.MatrixDev.Roomigrant:RoomigrantCompiler:$version"
    }
}
