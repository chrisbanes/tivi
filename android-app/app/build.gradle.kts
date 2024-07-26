// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
  id("app.tivi.android.application")
  id("app.tivi.kotlin.android")
  id("app.tivi.compose")
  id("androidx.baselineprofile")
}

android {
  namespace = "app.tivi"

  defaultConfig {
    applicationId = "app.tivi"
    versionCode = properties["TIVI_VERSIONCODE"]?.toString()?.toInt() ?: 19000
    versionName = "1.0"

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
      if (rootProject.file("release/app-release.jks").exists()) {
        storeFile = rootProject.file("release/app-release.jks")
        storePassword = properties["TIVI_RELEASE_KEYSTORE_PWD"]?.toString() ?: ""
        keyAlias = "tivi"
        keyPassword = properties["TIVI_RELEASE_KEY_PWD"]?.toString() ?: ""
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
  }

  buildTypes {
    debug {
      signingConfig = signingConfigs["debug"]
      versionNameSuffix = "-dev"
    }

    release {
      signingConfig = signingConfigs.findByName("release") ?: signingConfigs["debug"]
      isShrinkResources = true
      isMinifyEnabled = true
      proguardFiles("proguard-rules.pro")
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

  @Suppress("UnstableApiUsage")
  testOptions {
    managedDevices {
      devices {
        create<ManagedVirtualDevice>("api34") {
          device = "Pixel 6"
          apiLevel = 34
          systemImageSource = "aosp"
        }
      }
    }
  }
}

androidComponents {
  // Ignore the standardDebug variant
  beforeVariants(
    selector()
      .withBuildType("debug")
      .withFlavor("mode" to "standard"),
  ) { variant ->
    variant.enable = false
  }

  onVariants(selector().withBuildType("release")) { variant ->
    variant.packaging.resources.run {
      // Exclude AndroidX version files. We only do this in the release build so that
      // Layout Inspector continues to work for debug
      excludes.add("META-INF/*.version")
      // Exclude the Firebase/Fabric/other random properties files
      excludes.addAll("/*.properties", "META-INF/*.properties")
    }
  }
}

dependencies {
  qaImplementation(projects.shared.qa)
  standardImplementation(projects.shared.prod)

  implementation(libs.androidx.activity.activity)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.browser)
  implementation(libs.androidx.profileinstaller)
  implementation(libs.androidx.splashscreen)

  qaImplementation(libs.leakCanary)

  implementation(libs.kotlin.coroutines.android)

  implementation(libs.google.firebase.crashlytics)

  "baselineProfile"(projects.androidApp.benchmark)

  androidTestImplementation(projects.androidApp.commonTest)
  androidTestImplementation(libs.androidx.uiautomator)
  androidTestImplementation(libs.screengrab)
  androidTestImplementation(libs.junit)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.rules)
}

baselineProfile {
  mergeIntoMain = true
  saveInSrc = true
}

if (file("google-services.json").exists()) {
  apply(plugin = libs.plugins.gms.googleServices.get().pluginId)
  apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
}

fun DependencyHandler.qaImplementation(dependencyNotation: Any) =
  add("qaImplementation", dependencyNotation)

fun DependencyHandler.standardImplementation(dependencyNotation: Any) =
  add("standardImplementation", dependencyNotation)
