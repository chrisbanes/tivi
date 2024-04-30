// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import java.io.File
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget

fun Project.configureIosLicensesTasks() {
  val xcodeTargetPlatform = providers.environmentVariable("PLATFORM_NAME").orElse("")
  val xcodeTargetArchs = providers.environmentVariable("ARCHS")
    .map { arch -> arch.split(",", " ").filter { it.isNotBlank() } }
    .orElse(emptyList())

  tasks.register<AssetCopyTask>("copyLicenseeOutputToIosBundle") {
    val targetName = xcodeTargetPlatform.zip(xcodeTargetArchs, ::Pair)
      .map { (targetPlatform, targetArchs) ->
        determineIosKonanTargetsFromEnv(targetPlatform, targetArchs)
          .mapTo(HashSet()) { it.presetName }
      }
      .map { it.firstOrNull().orEmpty() }

    val output = buildOutputFile()

    if (!targetName.isPresent || output == null) {
      // If we don't have a target or output dir, disable the task and skip the rest of setup
      enabled = false
      return@register
    }

    inputFile.set(
      layout.buildDirectory.zip(targetName) { buildDir, target ->
        buildDir.file("reports/licensee/$target/artifacts.json")
      },
    )

    outputDirectory.set(output)
    outputFilename.set("licenses.json")

    dependsOn("licensee${targetName.get().capitalized()}")
  }

  tasks
    .matching { it.name == "embedAndSignAppleFrameworkForXcode" }
    .configureEach { dependsOn("copyLicenseeOutputToIosBundle") }
}

private fun buildOutputFile(): File? {
  return runCatching {
    File(System.getenv("BUILT_PRODUCTS_DIR"))
      .resolve(System.getenv("CONTENTS_FOLDER_PATH"))
  }.getOrNull()
}

internal fun determineIosKonanTargetsFromEnv(
  platform: String,
  archs: List<String>,
): List<KonanTarget> {
  if (platform.isEmpty() || archs.isEmpty()) {
    return emptyList()
  }

  val targets: MutableSet<KonanTarget> = mutableSetOf()

  when {
    platform.startsWith("iphoneos") -> {
      targets.addAll(
        archs.map { arch ->
          when (arch) {
            "arm64", "arm64e" -> KonanTarget.IOS_ARM64
            else -> error("Unknown iOS device arch: '$arch'")
          }
        },
      )
    }

    platform.startsWith("iphonesimulator") -> {
      targets.addAll(
        archs.map { arch ->
          when (arch) {
            "arm64", "arm64e" -> KonanTarget.IOS_SIMULATOR_ARM64
            "x86_64" -> KonanTarget.IOS_X64
            else -> error("Unknown iOS simulator arch: '$arch'")
          }
        },
      )
    }

    else -> error("Unknown iOS platform: '$platform'")
  }

  return targets.toList()
}

private val KonanTarget.presetName: String
  get() {
    val nameParts = name.split('_').mapNotNull { it.takeIf(String::isNotEmpty) }
    return nameParts.asSequence()
      .drop(1)
      .joinToString("", nameParts.firstOrNull().orEmpty()) { it.capitalized() }
  }
