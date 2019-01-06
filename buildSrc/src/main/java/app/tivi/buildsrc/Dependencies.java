/*
 * Copyright 2019 Google, Inc.
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

package app.tivi.buildsrc;

interface Versions {
    String ktlint = "0.29.0";
}

interface Libs {
    String androidGradlePlugin = "com.android.tools.build:gradle:3.3.0";
    String dexcountGradlePlugin = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.8.5";
    String playPublisherPlugin = "com.github.triplet.gradle:play-publisher:2.0.0";

    String mvRx = "com.airbnb.android:mvrx:0.6.0";

    String threeTenBp = "org.threeten:threetenbp:1.3.8";
    String threeTenBpNoTzdb = "org.threeten:threetenbp:1.3.8:no-tzdb";
    String threeTenAbp = "com.jakewharton.threetenabp:threetenabp:1.1.1";

    String gravitySnapHelper = "com.github.rubensousa:gravitysnaphelper:2.0";

    String rxLint = "nl.littlerobots.rxlint:rxlint:1.7.2";

    String timber = "com.jakewharton.timber:timber:4.7.1";

    String tmdbJava = "com.uwetrottmann.tmdb2:tmdb-java:1.10.1";

    String appauth = "net.openid:appauth:0.7.1";

    String junit = "junit:junit:4.12";
    String robolectric = "org.robolectric:robolectric:4.1";
    String mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0";

    interface Google {
        String material = "com.google.android.material:material:1.1.0-alpha02";
        String firebaseCore = "com.google.firebase:firebase-core:16.0.4";
        String crashlytics = "com.crashlytics.sdk.android:crashlytics:2.9.8";
        String gmsGoogleServices = "com.google.gms:google-services:4.2.0";
        String fabricPlugin = "io.fabric.tools:gradle:1.27.0";
    }

    interface Kotlin {
        String version = "1.3.20-eap-52";
        String stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:" + version;
        String reflect = "org.jetbrains.kotlin:kotlin-reflect:" + version;
        String gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:" + version;
        String extensions = "org.jetbrains.kotlin:kotlin-android-extensions:" + version;
    }

    interface Coroutines {
        String version = "1.1.0";
        String core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:" + version;
        String rx2 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:" + version;
        String android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:" + version;
    }

    interface AndroidX {
        String appcompat = "androidx.appcompat:appcompat:1.0.2";
        String browser = "androidx.browser:browser:1.0.0";
        String palette = "androidx.palette:palette:1.0.0";
        String recyclerview = "androidx.recyclerview:recyclerview:1.0.0";
        String emoji = "androidx.emoji:emoji:1.0.0";
        String fragment = "androidx.fragment:fragment:1.0.0";

        interface Test {
            String core = "androidx.test:core:1.1.0";
            String runner = "androidx.test:runner:1.1.1";
            String rules = "androidx.test:rules:1.1.1";

            String espressoCore = "androidx.test.espresso:espresso-core:3.1.1";
        }

        String archCoreTesting = "androidx.arch.core:core-testing:2.0.0";

        interface Paging {
            String version = "2.0.0";
            String common = "androidx.paging:paging-common:" + version;
            String runtime = "androidx.paging:paging-runtime:" + version;
            String rxjava2 = "androidx.paging:paging-rxjava2:" + version;
        }

        String preference = "androidx.preference:preference:1.1.0-alpha02";

        String constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.0-alpha2";

        String coreKtx = "androidx.core:core-ktx:1.0.1";

        interface Lifecycle {
            String version = "2.0.0";
            String extensions = "androidx.lifecycle:lifecycle-extensions:" + version;
            String reactive = "androidx.lifecycle:lifecycle-reactivestreams:" + version;
            String compiler = "androidx.lifecycle:lifecycle-compiler:" + version;
        }

        interface Room {
            String version = "2.0.0";
            String common = "androidx.room:room-common:" + version;
            String runtime = "androidx.room:room-runtime:" + version;
            String rxjava2 = "androidx.room:room-rxjava2:" + version;
            String compiler = "androidx.room:room-compiler:" + version;
        }

        interface Work {
            String version = "1.0.0-beta01";
            String runtimeKtx = "android.arch.work:work-runtime-ktx:" + version;
        }
    }

    interface RxJava {
        String rxJava = "io.reactivex.rxjava2:rxjava:2.2.5";
        String rxKotlin = "io.reactivex.rxjava2:rxkotlin:2.3.0";
        String rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.0";
    }

    interface Dagger {
        String version = "2.20";
        String dagger = "com.google.dagger:dagger:" + version;
        String androidSupport = "com.google.dagger:dagger-android-support:" + version;
        String compiler = "com.google.dagger:dagger-compiler:" + version;
        String androidProcessor = "com.google.dagger:dagger-android-processor:" + version;
    }

    interface Glide {
        String version = "4.8.0";
        String glide = "com.github.bumptech.glide:glide:" + version;
        String compiler = "com.github.bumptech.glide:compiler:" + version;
    }

    interface Retrofit {
        String version = "2.3.0";
        String retrofit = "com.squareup.retrofit2:retrofit:" + version;
        String retrofit_rxjava_adapter = "com.squareup.retrofit2:adapter-rxjava2:" + version;
        String gsonConverter = "com.squareup.retrofit2:converter-gson:" + version;
    }

    interface OkHttp {
        String loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:3.12.1";
    }

    interface Epoxy {
        String version = "3.1.0";
        String epoxy = "com.airbnb.android:epoxy:" + version;
        String paging = "com.airbnb.android:epoxy-paging:" + version;
        String dataBinding = "com.airbnb.android:epoxy-databinding:" + version;
        String processor = "com.airbnb.android:epoxy-processor:" + version;
    }

    interface AssistedInject {
        String version = "0.3.2";
        String annotationDagger2 = "com.squareup.inject:assisted-inject-annotations-dagger2:" + version;
        String processorDagger2 = "com.squareup.inject:assisted-inject-processor-dagger2:" + version;
    }
}
