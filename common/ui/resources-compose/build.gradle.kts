// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.android.library")
    id("app.tivi.android.compose")
    id("app.tivi.kotlin.android")
}

android {
    namespace = "dev.icerock.moko.resources.compose"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(platform(libs.compose.bom))
    implementation(libs.compose.foundation.foundation)

    api(libs.moko.resources)
}
