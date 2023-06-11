// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

fun Project.configureLauncherTasks() {
    androidComponents {
        onVariants { variant ->
            tasks.register("open${variant.name.capitalize()}") {
                dependsOn(tasks.named("install${variant.name.capitalize()}"))

                doLast {
                    exec {
                        commandLine = "adb shell monkey -p ${variant.applicationId.get()} -c android.intent.category.LAUNCHER 1".split(" ")
                    }
                }
            }
        }
    }
}

private fun Project.androidComponents(action: ApplicationAndroidComponentsExtension.() -> Unit) =
    extensions.configure<ApplicationAndroidComponentsExtension>(action)
