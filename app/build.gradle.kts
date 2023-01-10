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


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.cacheFixPlugin)
}

val appVersionCode = propOrDef("TIVI_VERSIONCODE", "17").toInt()
println("APK version code: $appVersionCode")

val useReleaseKeystore = rootProject.file("release/app-release.jks").exists()

android {
    namespace = "app.tivi"

    defaultConfig {
        applicationId = "app.tivi"
        versionCode = appVersionCode
        versionName = "0.7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "TRAKT_CLIENT_ID", "\"" + propOrDef("TIVI_TRAKT_CLIENT_ID", "") + "\"")
        buildConfigField("String", "TRAKT_CLIENT_SECRET", "\"" + propOrDef("TIVI_TRAKT_CLIENT_SECRET", "") + "\"")
        buildConfigField("String", "TMDB_API_KEY", "\"" + propOrDef("TIVI_TMDB_API_KEY", "") + "\"")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("release/app-debug.jks")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        create("release") {
            if (useReleaseKeystore) {
                storeFile = rootProject.file("release/app-release.jks")
                storePassword = propOrDef("TIVI_RELEASE_KEYSTORE_PWD", "")
                keyAlias = "tivi"
                keyPassword = propOrDef("TIVI_RELEASE_KEY_PWD", "")
            }
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        // Disable lintVital. Not needed since lint is run on CI
        checkReleaseBuilds = false
        // Ignore any tests
        ignoreTestSources = true
        // Make the build fail on any lint errors
        abortOnError = true
        // Allow lint to check dependencies
        checkDependencies = true
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composecompiler.get()
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packagingOptions {
        packagingOptions.resources.excludes += setOf(
            // Exclude AndroidX version files
            "META-INF/*.version",
            // Exclude consumer proguard files
            "META-INF/proguard/*",
            // Exclude the Firebase/Fabric/other random properties files
            "/*.properties",
            "fabric/*.properties",
            "META-INF/*.properties",
        )
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs["debug"]
            versionNameSuffix = "-dev"
            applicationIdSuffix = ".debug"
        }

        release {
            signingConfig = signingConfigs[if (useReleaseKeystore) "release" else "debug"]
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }

        create("benchmark") {
            initWith(buildTypes["release"])
            signingConfig = signingConfigs["debug"]
            matchingFallbacks += "release"
            proguardFiles("benchmark-rules.pro")
        }
    }

    flavorDimensions += "mode"
    productFlavors {
        create("qa") {
            dimension = "mode"
            // This is a build with Chucker enabled
            proguardFiles("proguard-rules-chucker.pro")
            versionNameSuffix = "-qa"
        }

        create("standard") {
            dimension = "mode"
            // Standard build is always ahead of the QA builds as it goes straight to
            // the alpha channel. This is the 'release' flavour
            versionCode = (android.defaultConfig.versionCode ?: 0) + 1
        }
    }

    variantFilter {
        // Ignore the standardDebug variant, QA == debug
        val isQa = flavors.any { it.name.contains("qa") }
        if (!isQa && buildType.name == "debug") {
            ignore = true
        } else if (isQa && buildType.name == "benchmark") {
            ignore = true
        }
    }
}

dependencies {
    implementation(projects.base)
    implementation(projects.core.analytics)
    implementation(projects.core.logging)
    implementation(projects.common.ui.view)
    implementation(projects.common.imageloading)
    implementation(projects.common.ui.compose)
    implementation(projects.data)
    implementation(projects.dataAndroid)
    implementation(projects.api.trakt)
    implementation(projects.api.traktAuth)
    implementation(projects.api.tmdb)
    implementation(projects.domain)
    implementation(projects.tasks)

    implementation(projects.ui.account)
    implementation(projects.ui.discover)
    implementation(projects.ui.showdetails)
    implementation(projects.ui.episodedetails)
    implementation(projects.ui.library)
    implementation(projects.ui.popular)
    implementation(projects.ui.trending)
    implementation(projects.ui.recommended)
    implementation(projects.ui.search)
    implementation(projects.ui.showseasons)
    implementation(projects.ui.settings)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.emoji)

    implementation(libs.compose.foundation.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material.material)
    implementation(libs.compose.material.iconsext)
    implementation(libs.compose.material3)
    implementation(libs.compose.animation.animation)
    implementation(libs.compose.ui.tooling)

    implementation(libs.accompanist.navigation.animation)
    implementation(libs.accompanist.navigation.material)

    implementation(libs.threeTenAbp)

    implementation(libs.timber)

    implementation(libs.kotlin.coroutines.android)

    implementation(libs.androidx.profileinstaller)

    implementation(libs.okhttp.loggingInterceptor)

    implementation(libs.hilt.library)
    implementation(libs.androidx.hilt.navigationcompose)
    kapt(libs.hilt.compiler)

    implementation(libs.google.crashlytics)
    implementation(libs.google.analytics)

    "qaImplementation"(libs.chucker.library)

    "qaImplementation"(libs.debugdrawer.debugdrawer)
    "qaImplementation"(libs.debugdrawer.retrofit)
    "qaImplementation"(libs.retrofit.mock)
    "qaImplementation"(libs.debugdrawer.timber)
    "qaImplementation"(libs.debugdrawer.okhttplogger)

    "qaImplementation"(libs.leakCanary)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.rules)
}

android.applicationVariants.forEach { variant ->
    tasks.create("open${variant.name.capitalize()}") {
        dependsOn(tasks.named("install${variant.name.capitalize()}"))

        doLast {
            exec {
                commandLine = "adb shell monkey -p ${variant.applicationId} -c android.intent.category.LAUNCHER 1".split(" ")
            }
        }
    }
}

if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.gms.googleServices.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)

    // Disable uploading mapping files for the benchmark build type
    android.buildTypes.getByName("benchmark") {
        configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
            mappingFileUploadEnabled = false
        }
    }
}

fun <T : Any> propOrDef(propertyName: String, defaultValue: T): T {
    @Suppress("UNCHECKED_CAST")
    val propertyValue = project.properties[propertyName] as T?
    return propertyValue ?: defaultValue
}
