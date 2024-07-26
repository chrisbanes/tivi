
plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.core.entitlements.core)
        api(projects.common.ui.compose)
      }
    }

    androidMain {
      dependencies {
        implementation(libs.revenuecat.ui)
      }
    }

    iosMain {
      dependencies {
        implementation(libs.revenuecat.ui)
      }
    }
  }
}

android {
  namespace = "app.tivi.core.entitlements.ui"
}
