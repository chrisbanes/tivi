// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.test")
  id("app.tivi.kotlin.android")
}

android {
  namespace = "app.tivi.benchmark"

  defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    create("benchmark") {
      isDebuggable = true
      signingConfig = signingConfigs["debug"]
      matchingFallbacks += "release"
    }
  }

  testOptions {
    managedDevices {
      devices {
        create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel5Api31") {
          device = "Pixel 5"
          apiLevel = 31
          systemImageSource = "aosp"
        }
      }
    }
  }

  targetProjectPath = ":android-app:app"
  experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
  implementation(libs.androidx.test.junit)
  implementation(libs.androidx.benchmark.macro)
  implementation(libs.androidx.uiautomator)
  implementation(libs.androidx.test.junit)
  implementation(libs.kotlin.coroutines.android)

  implementation(projects.androidApp.commonTest)
}

androidComponents {
  beforeVariants(selector().all()) {
    it.enable = it.buildType == "benchmark"
  }
}
