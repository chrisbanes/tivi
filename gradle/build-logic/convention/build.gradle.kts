// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


plugins {
  `kotlin-dsl`
  alias(libs.plugins.spotless)
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

spotless {
  kotlin {
    target("src/**/*.kt")
    ktlint()
    licenseHeaderFile(rootProject.file("../../spotless/cb-copyright.txt"))
  }

  kotlinGradle {
    target("*.kts")
    ktlint()
    licenseHeaderFile(rootProject.file("../../spotless/cb-copyright.txt"), "(^(?![\\/ ]\\**).*$)")
  }
}

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.spotless.gradlePlugin)
  compileOnly(libs.compose.gradlePlugin)
  compileOnly(libs.composeCompiler.gradlePlugin)
  compileOnly(libs.licensee.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("kotlinMultiplatform") {
      id = "app.tivi.kotlin.multiplatform"
      implementationClass = "app.tivi.gradle.KotlinMultiplatformConventionPlugin"
    }

    register("root") {
      id = "app.tivi.root"
      implementationClass = "app.tivi.gradle.RootConventionPlugin"
    }

    register("kotlinAndroid") {
      id = "app.tivi.kotlin.android"
      implementationClass = "app.tivi.gradle.KotlinAndroidConventionPlugin"
    }

    register("androidApplication") {
      id = "app.tivi.android.application"
      implementationClass = "app.tivi.gradle.AndroidApplicationConventionPlugin"
    }

    register("androidLibrary") {
      id = "app.tivi.android.library"
      implementationClass = "app.tivi.gradle.AndroidLibraryConventionPlugin"
    }

    register("androidTest") {
      id = "app.tivi.android.test"
      implementationClass = "app.tivi.gradle.AndroidTestConventionPlugin"
    }

    register("compose") {
      id = "app.tivi.compose"
      implementationClass = "app.tivi.gradle.ComposeMultiplatformConventionPlugin"
    }
  }
}
