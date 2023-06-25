// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

fun Project.configureAndroidCompose() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    android {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = libs.findVersion("composecompiler").get().toString()
        }
    }
}

private fun Project.android(action: CommonExtension<*, *, *, *>.() -> Unit) =
    extensions.configure(CommonExtension::class, action)
