// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.android.application")
        apply("org.gradle.android.cache-fix")
      }

      configureAndroid()
      configureLauncherTasks()
      configureLicensee()
      configureAndroidLicensesTasks()
    }
  }
}
