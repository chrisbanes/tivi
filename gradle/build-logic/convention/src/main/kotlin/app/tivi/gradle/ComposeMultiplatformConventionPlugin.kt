// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("org.jetbrains.compose")
    configureCompose()
  }
}

fun Project.configureCompose() {
  compose {
    // kotlinCompilerPlugin.set(libs.findVersion("compose-compiler").get().requiredVersion)

    kotlinCompilerPluginArgs.addAll(
      // Enable 'strong skipping'
      // https://medium.com/androiddevelopers/jetpack-compose-strong-skipping-mode-explained-cbdb2aa4b900
      "experimentalStrongSkipping=true",
    )
  }
}

fun Project.compose(block: ComposeExtension.() -> Unit) {
  extensions.configure<ComposeExtension>(block)
}
