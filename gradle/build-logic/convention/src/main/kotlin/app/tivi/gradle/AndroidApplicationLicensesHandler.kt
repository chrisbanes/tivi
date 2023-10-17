// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

fun Project.configureAndroidLicensesTasks() {
  androidComponents {
    onVariants { variant ->
      val capitalizedVariantName = variant.name.capitalized()

      val outputDir = objects.directoryProperty()

      val copyArtifactsTask = tasks.register<Copy>(
        "copy${capitalizedVariantName}LicenseeOutputToAndroidAssets",
      ) {
        from(
          layout.buildDirectory
            .file("reports/licensee/android$capitalizedVariantName/artifacts.json"),
        )
        into(outputDir.file("licenses.json"))

        dependsOn("licenseeAndroid$capitalizedVariantName")
      }

      variant.sources.assets?.addGeneratedSourceDirectory(copyArtifactsTask) { outputDir }
    }
  }
}

private fun Project.androidComponents(action: ApplicationAndroidComponentsExtension.() -> Unit) =
  extensions.configure(action)
