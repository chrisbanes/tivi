// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("org.jetbrains.compose")
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    configureCompose()
  }
}

fun Project.configureCompose() {
  composeCompiler {
    // Enable 'strong skipping'
    // https://medium.com/androiddevelopers/jetpack-compose-strong-skipping-mode-explained-cbdb2aa4b900
    enableStrongSkippingMode.set(true)

    // Needed for Layout Inspector to be able to see all of the nodes in the component tree:
    //https://issuetracker.google.com/issues/338842143
    includeSourceInformation.set(true)

    if (project.providers.gradleProperty("tivi.enableComposeCompilerReports").isPresent) {
      val composeReports = layout.buildDirectory.map { it.dir("reports").dir("compose") }
      reportsDestination.set(composeReports)
      metricsDestination.set(composeReports)
    }

    // https://github.com/chrisbanes/tivi/issues/1867
    tasks.matching { it.name == "syncComposeResourcesForIos" }
      .configureEach { enabled = false }

    stabilityConfigurationFile.set(rootProject.file("compose-stability.conf"))
  }

  // Workaround for:
  // Task 'generateDebugUnitTestLintModel' uses this output of task
  // 'generateResourceAccessorsForAndroidUnitTest' without declaring an explicit or
  // implicit dependency.
  tasks.matching { it is AndroidLintAnalysisTask || it is LintModelWriterTask }.configureEach {
    mustRunAfter(tasks.matching { it.name.startsWith("generateResourceAccessorsFor") })
  }
}

fun Project.composeCompiler(block: ComposeCompilerGradlePluginExtension.() -> Unit) {
  extensions.configure<ComposeCompilerGradlePluginExtension>(block)
}
