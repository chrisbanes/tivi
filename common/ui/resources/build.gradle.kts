// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
    id("app.tivi.kotlin.multiplatform")
    id("app.tivi.android.library")
    alias(libs.plugins.moko.resources) // needs to be enabled after AGP
}

kotlin {
    jvm()
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.data.models)
                api(libs.moko.resources)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.assertk)
            }
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "app.tivi.common.ui.resources"
}

android {
    namespace = "app.tivi.common.ui.resources"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        // Disable warnings as errors
        allWarningsAsErrors.set(false)
    }
}

tasks.withType(com.android.build.gradle.tasks.MergeResources::class).configureEach {
    dependsOn(tasks.getByPath("generateMRandroidMain"))
}

tasks.withType(com.android.build.gradle.tasks.MapSourceSetPathsTask::class).configureEach {
    dependsOn(tasks.getByPath("generateMRandroidMain"))
}
