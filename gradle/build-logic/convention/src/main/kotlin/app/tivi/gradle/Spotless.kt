// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

fun Project.configureSpotless() {
  // Skip Spotless for thirdparty code
  if (path.startsWith(":thirdparty")) return

  with(pluginManager) {
    apply("com.diffplug.spotless")
  }

  spotless {
    val ktlintVersion = libs.findLibrary("ktlint").get().get().version

    kotlin {
      target("src/**/*.kt")
      ktlint(ktlintVersion)
      licenseHeaderFile(rootProject.file("spotless/google-copyright.txt"))
        .named("google")
        .onlyIfContentMatches("Copyright \\d+,* Google")
      licenseHeaderFile(rootProject.file("spotless/cb-copyright.txt"))
        .named("cb-existing")
        .onlyIfContentMatches("Copyright \\d+,* Christopher Banes")
      licenseHeaderFile(rootProject.file("spotless/cb-copyright.txt"))
        .named("cb-none")
        .onlyIfContentMatches("^(?!// Copyright).*\$")
    }

    kotlinGradle {
      target("*.kts")
      ktlint(ktlintVersion)
      licenseHeaderFile(rootProject.file("spotless/google-copyright.txt"), "(^(?![\\/ ]\\**).*$)")
        .named("google")
        .onlyIfContentMatches("Copyright \\d+,* Google")
      licenseHeaderFile(rootProject.file("spotless/cb-copyright.txt"), "(^(?![\\/ ]\\**).*$)")
        .named("cb-existing")
        .onlyIfContentMatches("Copyright \\d+,* Christopher Banes")
      licenseHeaderFile(rootProject.file("spotless/cb-copyright.txt"), "(^(?![\\/ ]\\**).*$)")
        .named("cb-none")
        .onlyIfContentMatches("^(?!// Copyright).*\$")
    }
  }
}

private fun Project.spotless(action: SpotlessExtension.() -> Unit) = extensions.configure<SpotlessExtension>(action)
