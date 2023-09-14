// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import app.tivi.gradle.task.AssetCopyTask
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.reporting.ReportingExtension
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

fun Project.configureAndroidLicensesTasks() {
    val reportingExtension = project.extensions.getByType(ReportingExtension::class.java)

    androidComponents {
        onVariants { variant ->
            val capitalizedVariantName = variant.name.capitalized()

            val artifactsFile = reportingExtension.file("licensee/${variant.name}/artifacts.json")

            val copyArtifactsTask = tasks.register<AssetCopyTask>(
                "copy${capitalizedVariantName}LicenseeOutputToAndroidAssets",
            ) {
                inputFile.set(artifactsFile)
                outputFilename.set("licenses.json")

                dependsOn("licensee$capitalizedVariantName")
            }

            variant.sources.assets?.addGeneratedSourceDirectory(
                copyArtifactsTask,
                AssetCopyTask::outputDirectory,
            )
        }
    }
}

private fun Project.androidComponents(action: ApplicationAndroidComponentsExtension.() -> Unit) =
    extensions.configure(action)
