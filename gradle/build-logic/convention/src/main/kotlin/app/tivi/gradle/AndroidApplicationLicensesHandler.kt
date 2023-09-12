// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import app.tivi.gradle.task.AssetCopyTask
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.io.File
import java.util.Locale
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

abstract class LicenseeAssetCreatorTask : DefaultTask() {

    @get:InputDirectory
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @ExperimentalStdlibApi
    @TaskAction
    fun taskAction() {
        val json = Json { ignoreUnknownKeys = true }
        val variantName = name.replace("create", "").replace("LicensesAsset", "")
        println(" variantName: $variantName")
        println(" outputDirectory: $outputDirectory")
        println(" outputDirectory: $inputDirectory")
//        project.layout.buildDirectory.dir("generated/dependencyAssets/").get().asFile.mkdirs()
//        layout.buildDirectory.dir("generated/dependencyAssets/")

//        val licenseeFile = project.extensions.getByType(ReportingExtension::class.java).file("licensee/${variantName}/artifacts.json")
        val licenseeFile = File(inputDirectory.get().asFile, "reports/licensee/${variantName}/artifacts.json")
        println(" licenseeFile: $licenseeFile")
        val fileContent = licenseeFile.readText()
        println(" fileContent: $fileContent")
        val licenseJsonList = json.decodeFromString<List<JsonObject>>(fileContent)
        val sortedLicenseJsonList = licenseJsonList
            .sortedBy { it["name"].toString().lowercase() }
        val jsonString = json.encodeToString(sortedLicenseJsonList)

        println(" jsonString: $jsonString")
        outputDirectory.get().asFile.mkdirs()
        File(outputDirectory.get().asFile, "licensee.json")
            .writeText(jsonString)
    }
}

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

            // Define a new task to generate the asset files. Here the file is only
            // copied, but you could do anything else instead.
//            val copyArtifactsTask =
//                tasks.register<Copy>("copy${variant.name.capitalize()}ArtifactList") {
//                    from(
//                        project.extensions.getByType(ReportingExtension::class.java)
//                            .file("licensee/${variant.name}/artifacts.json")
//                    )
//                    into(layout.buildDirectory.dir("generated/dependencyAssets/"))
//                }
//
//            // This dependency is only necessary if the asset generation depends on
//            // something else, the output of the licensee plugin in my case.
//            copyArtifactsTask.dependsOn("licensee${variant.name.capitalize()}")
////
////            // Add a dependency between the asset merging and our generation task.
////            // This is necessary to ensure that the assets are generated prior to
////            // the merging step.
////            tasks.named("merge${variant.name.capitalize()}Assets").dependsOn(copyArtifactsTask)
//            tasks["merge${variant.name.capitalize()}Assets"].dependsOn(copyArtifactsTask)

//            println(" variant.sources.assets: ${variant.sources.assets?.all}")
//            val assetCreationTask =
//                project.tasks.register<LicenseeAssetCreatorTask>(
//                    "create${variant.name.capitalize()}LicensesAsset",
//                ){
//                    inputDirectory.set(
//                        project.layout.buildDirectory
//                    )
////                    outputDirectory.set(
////                        variant.sources.assets?.srcDir(
////                            layout.buildDirectory.dir("generated/dependencyAssets/")
////                        )
////                    )
//                }
//            variant.sources.assets?.addGeneratedSourceDirectory(
//                assetCreationTask,
//                LicenseeAssetCreatorTask::outputDirectory,
//            )
//
//            assetCreationTask.dependsOn("licensee${variant.name.capitalize()}")
////            val mergeAssets = project.tasks.findByPath(":android-app:app:merge${variant.name.capitalize()}Assets")
//////            Task mergeAssets = project.tasks.findByPath(":${mainModuleName}:merge${variant.name.capitalize()}Assets")
////            if (mergeAssets != null) {
////                mergeAssets.dependsOn(assetCreationTask)
////            }
//            tasks[":merge${variant.name.capitalize()}Assets"].dependsOn(assetCreationTask)


        }
    }
}

private fun Project.androidComponents(action: ApplicationAndroidComponentsExtension.() -> Unit) =
    extensions.configure<ApplicationAndroidComponentsExtension>(action)
