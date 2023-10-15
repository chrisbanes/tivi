// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

fun Project.configureKotlin() {
    // Configure Java to use our chosen language level. Kotlin will automatically pick this up
    configureJava()

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            languageVersion.set(KotlinVersion.KOTLIN_1_9)
        }
    }
}
