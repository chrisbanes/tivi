
plugins {
  id("app.tivi.android.library")
  id("app.tivi.kotlin.multiplatform")
  alias(libs.plugins.buildConfig)
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.core.base)
      }
    }

    val mobileMain by creating {
      dependsOn(commonMain.get())

      dependencies {
        api(libs.revenuecat.core)
        implementation(libs.revenuecat.datetime)
        implementation(libs.revenuecat.result)

        api(projects.core.logging.api)
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

buildConfig {
  packageName("app.tivi.entitlements")

  buildConfigField(
    type = String::class.java,
    name = "TIVI_REVENUECAT_ANDROID_API_KEY",
    value = provider { properties["TIVI_REVENUECAT_ANDROID_API_KEY"]?.toString() ?: "" },
  )

  buildConfigField(
    type = String::class.java,
    name = "TIVI_REVENUECAT_IOS_API_KEY",
    value = provider { properties["TIVI_REVENUECAT_IOS_API_KEY"]?.toString() ?: "" },
  )
}

android {
  namespace = "app.tivi.core.entitlements.core"
}
