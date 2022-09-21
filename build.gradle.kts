/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import app.tivi.buildsrc.DependencyUpdates
import app.tivi.buildsrc.ReleaseType
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import dagger.hilt.android.plugin.HiltExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.napt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.gms.googleServices) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.dependencyUpdate)
}

allprojects {
    repositories {
        google()
        mavenCentral()

        // Jetpack Compose SNAPSHOTs
        val composeSnapshot = rootProject.libs.versions.composesnapshot.get()
        if (composeSnapshot.length > 1) {
            maven("https://androidx.dev/snapshots/builds/$composeSnapshot/artifacts/repository/")
        }

        // Used for snapshots
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

subprojects {
    apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
    spotless {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")

            ktlint(libs.versions.ktlint.get())
                .editorConfigOverride(mapOf("disabled_rules" to "filename"))
            licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            // Treat all Kotlin warnings as errors
            allWarningsAsErrors = true

            // Enable experimental coroutines APIs, including Flow
            freeCompilerArgs += listOf(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlin.Experimental"
            )

            if (project.hasProperty("tivi.enableComposeCompilerReports")) {
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                        project.buildDir.absolutePath + "/compose_metrics"
                )
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                        project.buildDir.absolutePath + "/compose_metrics"
                )
            }

            // Set JVM target to 11
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }

    plugins.withId(rootProject.libs.plugins.hilt.get().pluginId) {
        // Had to turn this off for napt to work
        extensions.getByType<HiltExtension>().enableAggregatingTask = false
    }
    plugins.withType<BasePlugin>().configureEach {
        extensions.configure<BaseExtension> {
            compileSdkVersion(libs.versions.compileSdk.get().toInt())
            defaultConfig {
                minSdk = libs.versions.minSdk.get().toInt()
                targetSdk = libs.versions.targetSdk.get().toInt()
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
}

/**
 * Update dependencyUpdates task to reject versions which are more 'unstable' than our
 * current version.
 */
tasks.withType<DependencyUpdatesTask>().configureEach {
    rejectVersionIf {
        val current = DependencyUpdates.versionToRelease(currentVersion)
        // If we're using a SNAPSHOT, ignore since we must be doing so for a reason.
        if (current == ReleaseType.SNAPSHOT) return@rejectVersionIf true

        // Otherwise we reject if the candidate is more 'unstable' than our version
        val candidate = DependencyUpdates.versionToRelease(candidate.version)
        return@rejectVersionIf candidate.isLessStableThan(current)
    }
}
