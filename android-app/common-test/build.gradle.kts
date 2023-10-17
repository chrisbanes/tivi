// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.android")
}

android {
  namespace = "app.tivi.app.test"
}

dependencies {
  implementation(projects.core.base)
  api(libs.androidx.uiautomator)
}
