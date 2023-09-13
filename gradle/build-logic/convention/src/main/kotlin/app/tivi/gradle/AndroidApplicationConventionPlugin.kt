// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import app.cash.licensee.LicenseeExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.reporting.ReportingExtension
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.gradle.android.cache-fix")
                apply("app.cash.licensee")
                configure<LicenseeExtension> {
                    allow("Apache-2.0")
                    allow("MIT")
                    allow("BSD-3-Clause")
                    allowUrl("https://developer.android.com/studio/terms.html")
                }
            }
            val reportingExtension: ReportingExtension =
                project.extensions.getByType(ReportingExtension::class.java)
            configureAndroid()
            configureLauncherTasks()
            configureLicensesTasks(reportingExtension)
        }
    }
}
