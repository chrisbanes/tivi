// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import java.io.File
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget

fun Project.configureIosLicensesTasks() {
  val xcodeTargetPlatform = providers.environmentVariable("PLATFORM_NAME").orElse("")
  val xcodeTargetArchs = providers.environmentVariable("ARCHS")
    .map { arch -> arch.split(",", " ").filter { it.isNotBlank() } }
    .orElse(emptyList())

  tasks.register<Copy>("copyLicenseeOutputToIosBundle") {
    val targetName = xcodeTargetPlatform.zip(xcodeTargetArchs, ::Pair)
      .map { (targetPlatform, targetArchs) ->
        determineIosKonanTargetsFromEnv(targetPlatform, targetArchs)
          .mapTo(HashSet()) { it.presetName }
      }
      .map { it.firstOrNull().orEmpty() }
      .get()

    if (targetName.isEmpty()) {
      // If we don't have a target, disable the test and skip the rest of setup
      enabled = false
      return@register
    }

    from(layout.buildDirectory.file("reports/licensee/$targetName/artifacts.json"))

    into(
      providers.environmentVariable("BUILT_PRODUCTS_DIR")
        .zip(providers.environmentVariable("CONTENTS_FOLDER_PATH")) { builtProductsDir, contentsFolderPath ->
          File(builtProductsDir)
            .resolve(contentsFolderPath)
            .resolve("licenses.json")
        }.flatMap {
          objects.fileProperty().apply { set(it) }
        },
    )

    dependsOn("licensee${targetName.capitalized()}")
  }

  tasks.named("embedAndSignAppleFrameworkForXcode") {
    dependsOn("copyLicenseeOutputToIosBundle")
  }
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
            "armv7", "armv7s" -> KonanTarget.IOS_ARM32
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
