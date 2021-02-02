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
    const val ktlint = "0.40.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.0.0-alpha05"

    const val threeTenBp = "org.threeten:threetenbp:1.5.0"
    const val threeTenBpNoTzdb = "$threeTenBp:no-tzdb"
    const val threeTenAbp = "com.jakewharton.threetenabp:threetenabp:1.3.0"

    const val timber = "com.jakewharton.timber:timber:4.7.1"

    const val tmdbJava = "com.uwetrottmann.tmdb2:tmdb-java:2.2.0"
    const val traktJava = "com.uwetrottmann.trakt5:trakt-java:6.8.0"

    /**
     * Using Jitpack until AndroidX migration is out.
     * See https://github.com/openid/AppAuth-Android/pull/508
     *
     * https://jitpack.io/#openid/AppAuth-Android
     */
    const val appauth = "com.github.openid:AppAuth-Android:7aa9bf3edd"

    const val junit = "junit:junit:4.13.1"
    const val robolectric = "org.robolectric:robolectric:4.4"
    const val mockK = "io.mockk:mockk:1.10.5"

    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.6"

    const val store = "com.dropbox.mobile.store:store4:4.0.0"

    object Accompanist {
        private const val version = "0.5.0"
        const val coil = "dev.chrisbanes.accompanist:accompanist-coil:$version"
        const val insets = "dev.chrisbanes.accompanist:accompanist-insets:$version"
    }

    object Google {
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx:17.3.0"
        const val analytics = "com.google.firebase:firebase-analytics-ktx:18.0.1"
        const val crashlyticsGradle = "com.google.firebase:firebase-crashlytics-gradle:2.4.1"

        const val gmsGoogleServices = "com.google.gms:google-services:4.3.4"
    }

    object Kotlin {
        private const val version = "1.4.21-2"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }

    object Coroutines {
        private const val version = "1.4.2"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object AndroidX {
        const val browser = "androidx.browser:browser:1.0.0"
        const val collection = "androidx.collection:collection-ktx:1.1.0"
        const val palette = "androidx.palette:palette:1.0.0"
        const val emoji = "androidx.emoji:emoji:1.1.0"

        object Navigation {
            private const val version = "2.3.2"
            const val fragment = "androidx.navigation:navigation-fragment-ktx:$version"
        }

        object Fragment {
            private const val version = "1.3.0-beta02"
            const val fragment = "androidx.fragment:fragment:$version"
            const val fragmentKtx = "androidx.fragment:fragment-ktx:$version"
        }

        object Test {
            private const val version = "1.3.0"
            const val core = "androidx.test:core:$version"
            const val rules = "androidx.test:rules:$version"

            object Ext {
                private const val version = "1.1.2"
                const val junit = "androidx.test.ext:junit-ktx:$version"
            }

            const val espressoCore = "androidx.test.espresso:espresso-core:3.3.0"
        }

        const val archCoreTesting = "androidx.arch.core:core-testing:2.1.0"

        object Paging {
            private const val version = "3.0.0-alpha12"
            const val common = "androidx.paging:paging-common-ktx:$version"
            const val runtime = "androidx.paging:paging-runtime-ktx:$version"

            const val compose = "androidx.paging:paging-compose:1.0.0-alpha04"
        }

        const val coreKtx = "androidx.core:core-ktx:1.5.0-beta01"

        object Lifecycle {
            private const val version = "2.3.0-rc01"
            const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            const val livedata = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        }

        object Room {
            private const val version = "2.3.0-alpha04"
            const val common = "androidx.room:room-common:$version"
            const val runtime = "androidx.room:room-runtime:$version"
            const val compiler = "androidx.room:room-compiler:$version"
            const val ktx = "androidx.room:room-ktx:$version"
            const val testing = "androidx.room:room-testing:$version"
        }

        object Work {
            private const val version = "2.4.0"
            const val runtimeKtx = "androidx.work:work-runtime-ktx:$version"
        }

        object Compose {
            const val snapshot = ""
            const val version = "1.0.0-alpha11"

            @get:JvmStatic
            val snapshotUrl: String
                get() = "https://androidx.dev/snapshots/builds/$snapshot/artifacts/repository/"

            const val compiler = "androidx.compose.compiler:compiler:$version"

            const val runtime = "androidx.compose.runtime:runtime:$version"
            const val livedata = "androidx.compose.runtime:runtime-livedata:$version"

            const val foundation = "androidx.compose.foundation:foundation:$version"
            const val layout = "androidx.compose.foundation:foundation-layout:$version"

            const val ui = "androidx.compose.ui:ui:$version"
            const val material = "androidx.compose.material:material:$version"

            const val animation = "androidx.compose.animation:animation:$version"

            const val tooling = "androidx.compose.ui:ui-tooling:$version"
            const val test = "androidx.compose.ui:ui-test-junit4:${version}"
        }

        object Hilt {
            private const val version = "1.0.0-alpha02"
            const val work = "androidx.hilt:hilt-work:$version"
            const val viewmodel = "androidx.hilt:hilt-lifecycle-viewmodel:$version"
            const val compiler = "androidx.hilt:hilt-compiler:$version"
        }
    }

    object Dagger {
        private const val version = "2.30.1"
        const val dagger = "com.google.dagger:dagger:$version"
        const val compiler = "com.google.dagger:dagger-compiler:$version"
    }

    object Hilt {
        // Can't update to 2.31 yet due to https://issuetracker.google.com/177649295
        private const val version = "2.30.1-alpha"
        const val library = "com.google.dagger:hilt-android:$version"
        const val compiler = "com.google.dagger:hilt-android-compiler:$version"
        const val testing = "com.google.dagger:hilt-android-testing:$version"
        const val gradlePlugin = "com.google.dagger:hilt-android-gradle-plugin:$version"
    }

    object Retrofit {
        private const val version = "2.9.0"
        const val retrofit = "com.squareup.retrofit2:retrofit:$version"
        const val gsonConverter = "com.squareup.retrofit2:converter-gson:$version"
    }

    object OkHttp {
        private const val version = "4.9.0"
        const val okhttp = "com.squareup.okhttp3:okhttp:$version"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Coil {
        private const val version = "1.1.1"
        const val coil = "io.coil-kt:coil:$version"
    }

    object AssistedInject {
        private const val version = "0.6.0"
        const val annotationDagger2 = "com.squareup.inject:assisted-inject-annotations-dagger2:$version"
        const val processorDagger2 = "com.squareup.inject:assisted-inject-processor-dagger2:$version"
    }

    object Roomigrant {
        /**
         * We use a fork which has been migrated to AndroidX Room
         */
        private const val version = "master-SNAPSHOT"
        const val library = "com.github.chrisbanes.Roomigrant:RoomigrantLib:$version"
        const val compiler = "com.github.chrisbanes.Roomigrant:RoomigrantCompiler:$version"
    }

    const val truth = "com.google.truth:truth:1.1"
}
