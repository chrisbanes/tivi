// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import org.jetbrains.compose.desktop.application.dsl.TargetFormat

// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

plugins {
  // We have to use KMP due to Moko-resources
  // https://github.com/icerockdev/moko-resources/issues/263
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

kotlin {
  sourceSets {
    jvmMain {
      dependencies {
        implementation(projects.shared.qa)
        implementation(compose.desktop.currentOs)
        implementation(libs.kotlin.coroutines.swing)
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "app.tivi.MainKt"

    nativeDistributions {
      modules("java.sql")
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "app.tivi"
      packageVersion = "1.0.0"
    }
  }
}
