// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import app.cash.licensee.LicenseeExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class LicenseePlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("app.cash.licensee")
        configure<LicenseeExtension> {
            allow("Apache-2.0")
            allow("MIT")
            allow("BSD-3-Clause")
            allowUrl("https://developer.android.com/studio/terms.html")
        }
    }
}
