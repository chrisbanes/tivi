
plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  id("app.tivi.compose")
}

kotlin {
  sourceSets {
    val mobileMain by creating {
      dependsOn(commonMain.get())

      dependencies {
        api(projects.core.entitlements.core)
        implementation(libs.revenuecat.ui)

        api(projects.common.ui.compose)
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
