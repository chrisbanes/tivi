// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
  id("app.tivi.android.test")
  id("app.tivi.kotlin.android")
  id("androidx.baselineprofile")
}

android {
  namespace = "app.tivi.benchmark"

  defaultConfig {
    minSdk = 28
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

  flavorDimensions += "mode"
  productFlavors {
    create("qa") { dimension = "mode" }
    create("standard") { dimension = "mode" }
  }

  targetProjectPath = ":android-app:app"
}

dependencies {
  implementation(libs.androidx.test.junit)
  implementation(libs.androidx.benchmark.macro)
  implementation(libs.androidx.uiautomator)
  implementation(libs.kotlin.coroutines.android)

  implementation(projects.androidApp.commonTest)
}

@Suppress("UnstableApiUsage")
baselineProfile {
  managedDevices += "api34"
  useConnectedDevices = false

  // Set this to true for debugging
  enableEmulatorDisplay = false
}
