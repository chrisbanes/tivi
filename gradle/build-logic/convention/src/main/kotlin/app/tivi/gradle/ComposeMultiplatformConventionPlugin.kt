// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.compose")
        configureCompose()
    }
}

fun Project.configureCompose() {
    with(extensions.getByType<ComposeExtension>()) {
        kotlinCompilerPlugin.set("1.4.8-beta")
        kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=1.9.0")
    }
}
