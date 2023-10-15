// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.compose")
        configureCompose()
    }
}

fun Project.configureCompose() {
    val composeVersion = libs.findVersion("compose-multiplatform").get().requiredVersion

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            val group = requested.group
            if (group.startsWith("org.jetbrains.compose") && !group.endsWith("compiler")) {
                useVersion(composeVersion)
            }
        }
    }
}
