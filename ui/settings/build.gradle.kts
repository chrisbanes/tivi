// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.kotlin.android")
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.tivi.settings"
}

dependencies {
    implementation(projects.core.base)
    implementation(projects.common.ui.resources)
    implementation(projects.common.ui.view)
    implementation(projects.core.powercontroller)
    implementation(projects.core.preferences)

    implementation(libs.androidx.activity.activity)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    ksp(libs.kotlininject.compiler)
}
