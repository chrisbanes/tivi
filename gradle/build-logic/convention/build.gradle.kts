// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = "app.tivi.kotlin.multiplatform"
            implementationClass = "app.tivi.gradle.KotlinMultiplatformConventionPlugin"
        }

        register("kotlinAndroid") {
            id = "app.tivi.kotlin.android"
            implementationClass = "app.tivi.gradle.KotlinAndroidConventionPlugin"
        }

        register("androidApplication") {
            id = "app.tivi.android.application"
            implementationClass = "app.tivi.gradle.AndroidApplicationConventionPlugin"
        }

        register("androidLibrary") {
            id = "app.tivi.android.library"
            implementationClass = "app.tivi.gradle.AndroidLibraryConventionPlugin"
        }

        register("androidTest") {
            id = "app.tivi.android.test"
            implementationClass = "app.tivi.gradle.AndroidTestConventionPlugin"
        }
    }
}
