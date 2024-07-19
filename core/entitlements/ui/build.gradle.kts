
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

    val mobileMain by creating {
      dependsOn(commonMain.get())

      dependencies {
        implementation(libs.revenuecat.ui)
      }
    }

    androidMain {
      dependsOn(mobileMain)
    }

    iosMain {
      dependsOn(mobileMain)
    }
  }
}

android {
  namespace = "app.tivi.core.entitlements.ui"
}
