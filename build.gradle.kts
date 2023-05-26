/*
 * Copyright 2017 Christopher Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.AndroidLintTask
import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.cacheFixPlugin) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.gms.googleServices) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.spotless) apply false
}

allprojects {
    apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")
            ktlint(libs.versions.ktlint.get())
            licenseHeaderFile(rootProject.file("spotless/google-copyright.txt"))
                .named("google")
                .onlyIfContentMatches("Copyright \\d+ Google")
            licenseHeaderFile(rootProject.file("spotless/cb-copyright.txt"))
                .named("cb-existing")
                .onlyIfContentMatches("Copyright \\d+ Christopher Banes")
        }
        kotlinGradle {
            target("**/*.kts")
            targetExclude("$buildDir/**/*.kts")
            ktlint(libs.versions.ktlint.get())
            licenseHeaderFile(rootProject.file("spotless/google-copyright.txt"), "(^(?![\\/ ]\\*).*$)")
                .named("google")
                .onlyIfContentMatches("Copyright \\d+ Google")
            licenseHeaderFile(rootProject.file("spotless/cb-copyright.txt"), "(^(?![\\/ ]\\*).*$)")
                .named("cb-existing")
                .onlyIfContentMatches("Copyright \\d+ Christopher Banes")
        }
    }

    // Workaround for https://issuetracker.google.com/issues/268961156
    tasks.withType<AndroidLintTask> {
        tasks.findByName("kspTestKotlin")?.let {
            dependsOn(it)
        }
    }
    tasks.withType<AndroidLintAnalysisTask> {
        tasks.findByName("kspTestKotlin")?.let {
            dependsOn(it)
        }
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            // Treat all Kotlin warnings as errors
            allWarningsAsErrors.set(true)

            // Enable experimental coroutines APIs, including Flow
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
            )

            if (project.hasProperty("tivi.enableComposeCompilerReports")) {
                freeCompilerArgs.addAll(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                        project.buildDir.absolutePath + "/compose_metrics",
                )
                freeCompilerArgs.addAll(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                        project.buildDir.absolutePath + "/compose_metrics",
                )
            }
        }
    }
}
