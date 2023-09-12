// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import app.tivi.gradle.task.AssetCopyTask
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.util.Locale
import org.gradle.api.Project
import org.gradle.api.reporting.ReportingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register


fun Project.configureLicensesTasks(reportingExtension: ReportingExtension) {

    androidComponents {

        onVariants { variant ->
            val capitalizedVariantName = variant.name.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.getDefault())
                } else {
                    it.toString()
                }
            }
            val artifactsFile = reportingExtension.file("licensee/${variant.name}/artifacts.json")

            val copyArtifactsTask =
                project.tasks.register<AssetCopyTask>("copy${capitalizedVariantName}LicenseeReportToAssets") {
                    inputFile.set(artifactsFile)
                    targetFileName.set("artifacts.json")
                }
            variant.sources.assets?.addGeneratedSourceDirectory(
                copyArtifactsTask,
                AssetCopyTask::outputDirectory
            )
            copyArtifactsTask.dependsOn("licensee$capitalizedVariantName")


        }
    }
}

private fun Project.androidComponents(action: ApplicationAndroidComponentsExtension.() -> Unit) =
    extensions.configure<ApplicationAndroidComponentsExtension>(action)
