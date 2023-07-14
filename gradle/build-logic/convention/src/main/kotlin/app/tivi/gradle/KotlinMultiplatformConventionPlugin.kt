// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

@OptIn(ExperimentalKotlinGradlePluginApi::class)
class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            targetHierarchy.default()

            jvm()
            if (pluginManager.hasPlugin("com.android.library")) {
                android()
            }

            listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64(),
            ).forEach { target ->
                target.binaries.framework {
                    baseName = path.substring(1).replace(':', '-')
                }
            }

            targets.withType<KotlinNativeTarget>().configureEach {
                binaries.all {
                    // Enable debug symbols:
                    // https://kotlinlang.org/docs/native-ios-symbolication.html
                    freeCompilerArgs += "-Xadd-light-debug=enable"

                    // Add linker flag for SQLite. See:
                    // https://github.com/touchlab/SQLiter/issues/77
                    linkerOpts("-lsqlite3")
                }
            }

            configureSpotless()
            configureKotlin()
        }
    }
}

fun Project.addKspDependencyForAllTargets(dependencyNotation: Any) = addKspDependencyForAllTargets("", dependencyNotation)
fun Project.addKspTestDependencyForAllTargets(dependencyNotation: Any) = addKspDependencyForAllTargets("Test", dependencyNotation)

private fun Project.addKspDependencyForAllTargets(
    configurationNameSuffix: String,
    dependencyNotation: Any,
) {
    val kmpExtension = extensions.getByType<KotlinMultiplatformExtension>()
    dependencies {
        kmpExtension.targets
            .asSequence()
            .filter { target ->
                // Don't add KSP for common target, only final platforms
                target.platformType != KotlinPlatformType.common
            }
            .forEach { target ->
                add(
                    "ksp${target.targetName.capitalized()}$configurationNameSuffix",
                    dependencyNotation,
                )
            }
    }
}
