// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

fun Project.configureAndroid() {
  android {
    compileSdkVersion(Versions.COMPILE_SDK)

    defaultConfig {
      minSdk = Versions.MIN_SDK
      targetSdk = Versions.TARGET_SDK
    }

    compileOptions {
      // https://developer.android.com/studio/write/java8-support
      isCoreLibraryDesugaringEnabled = true
    }
  }

  dependencies {
    // https://developer.android.com/studio/write/java8-support
    "coreLibraryDesugaring"(libs.findLibrary("tools.desugarjdklibs").get())
  }
}

private fun Project.android(action: BaseExtension.() -> Unit) = extensions.configure<BaseExtension>(action)
