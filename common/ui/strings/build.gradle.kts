// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import app.tivi.gradle.addKspDependencyForCommon

plugins {
    id("app.tivi.kotlin.multiplatform")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.ksp)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.lyricist.library)
                api(compose.foundation)
            }
        }
    }
}

ksp {
    arg("lyricist.packageName", "app.tivi.common.ui.resources")
}

addKspDependencyForCommon(libs.lyricist.processor)
