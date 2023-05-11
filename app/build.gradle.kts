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


import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.cacheFixPlugin)
    alias(libs.plugins.ksp)
}

val appVersionCode = propOrDef("TIVI_VERSIONCODE", "1000").toInt()
println("APK version code: $appVersionCode")

val useReleaseKeystore = rootProject.file("release/app-release.jks").exists()

android {
    namespace = "app.tivi"

    defaultConfig {
        applicationId = "app.tivi"
        versionCode = appVersionCode
        versionName = "0.9.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    packaging {
        resources.excludes += setOf(
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

            buildConfigField("String", "TRAKT_CLIENT_ID", "\"" + propOrDef("TIVI_DEBUG_TRAKT_CLIENT_ID", "TIVI_TRAKT_CLIENT_ID", "") + "\"")
            buildConfigField("String", "TRAKT_CLIENT_SECRET", "\"" + propOrDef("TIVI_DEBUG_TRAKT_CLIENT_SECRET", "TIVI_TRAKT_CLIENT_SECRET", "") + "\"")
            buildConfigField("String", "TMDB_API_KEY", "\"" + propOrDef("TIVI_DEBUG_TMDB_API_KEY", "TIVI_TMDB_API_KEY", "") + "\"")
        }

        release {
            signingConfig = signingConfigs[if (useReleaseKeystore) "release" else "debug"]
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")

            buildConfigField("String", "TRAKT_CLIENT_ID", "\"" + propOrDef("TIVI_TRAKT_CLIENT_ID", "") + "\"")
            buildConfigField("String", "TRAKT_CLIENT_SECRET", "\"" + propOrDef("TIVI_TRAKT_CLIENT_SECRET", "") + "\"")
            buildConfigField("String", "TMDB_API_KEY", "\"" + propOrDef("TIVI_TMDB_API_KEY", "") + "\"")
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
            applicationIdSuffix = ".qa"
        }

        create("standard") {
            dimension = "mode"
            // Standard build is always ahead of the QA builds as it goes straight to
            // the alpha channel. This is the 'release' flavour
            versionCode = (android.defaultConfig.versionCode ?: 0) + 1
        }
    }
}

androidComponents {
    // Ignore the QA Benchmark variant
    val qaBenchmark = selector()
        .withBuildType("benchmark")
        .withFlavor("mode" to "qa")
    beforeVariants(qaBenchmark) { variant ->
        variant.enable = false
    }

    // Ignore the standardDebug variant
    val standard = selector()
        .withBuildType("debug")
        .withFlavor("mode" to "standard")
    beforeVariants(standard) { variant ->
        variant.enable = false
    }
}

tasks.withType<KotlinCompilationTask<*>> {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi")
    }
}

dependencies {
    implementation(projects.core.base)
    implementation(projects.core.analytics)
    implementation(projects.core.logging)
    implementation(projects.core.performance)
    implementation(projects.core.powercontroller)
    implementation(projects.core.preferences)
    implementation(projects.common.ui.view)
    implementation(projects.common.imageloading)
    implementation(projects.common.ui.compose)
    implementation(projects.data.dbSqldelight)
    implementation(projects.api.trakt)
    implementation(projects.api.tmdb)
    implementation(projects.domain)
    implementation(projects.tasks.android)

    implementation(projects.ui.account)
    implementation(projects.ui.discover)
    implementation(projects.ui.episode.details)
    implementation(projects.ui.episode.track)
    implementation(projects.ui.library)
    implementation(projects.ui.popular)
    implementation(projects.ui.trending)
    implementation(projects.ui.recommended)
    implementation(projects.ui.search)
    implementation(projects.ui.show.details)
    implementation(projects.ui.show.seasons)
    implementation(projects.ui.settings)
    implementation(projects.ui.upnext)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.activity.activity)
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

    lintChecks(libs.slack.lint.compose)

    implementation(libs.accompanist.navigation.animation)
    implementation(libs.accompanist.navigation.material)

    implementation(libs.timber)

    implementation(libs.kotlin.coroutines.android)

    implementation(libs.androidx.profileinstaller)

    implementation(libs.okhttp.loggingInterceptor)

    ksp(libs.kotlininject.compiler)

    implementation(libs.google.firebase.crashlytics)

    qaImplementation(libs.chucker.library)

    qaImplementation(libs.debugdrawer.debugdrawer)
    qaImplementation(libs.debugdrawer.timber)
    qaImplementation(libs.debugdrawer.okhttplogger)

    qaImplementation(libs.leakCanary)

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

fun <T : Any> propOrDef(propertyName: String, fallbackProperty: String, defaultValue: T): T {
    @Suppress("UNCHECKED_CAST")
    return project.properties[propertyName] as T?
        ?: project.properties[fallbackProperty] as T?
        ?: defaultValue
}

fun DependencyHandler.qaImplementation(dependencyNotation: Any) =
    add("qaImplementation", dependencyNotation)
